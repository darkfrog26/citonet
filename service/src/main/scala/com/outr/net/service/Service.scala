package com.outr.net.service

import com.outr.net.Method
import com.outr.net.http.content.{ContentType, StringContent}
import com.outr.net.http.handler.PathMappingHandler
import com.outr.net.http.session.Session
import com.outr.net.http.{WebApplication, HttpHandler}
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import org.json4s.JsonAST.JString

import org.powerscala.json._
import org.powerscala.log.Logging
import org.powerscala.property.Property
import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
abstract class Service[In, Out](implicit inManifest: Manifest[In], outManifest: Manifest[Out]) extends HttpHandler with Logging {
  Service     // Make sure companion is initialized

  val pretty = Property[Boolean](default = Some(false))

  val inHttpRequestCaseValue = inManifest.runtimeClass.caseValues.find(cv => cv.valueType.hasType(classOf[HttpRequest]))
  val inHttpResponseCaseValue = inManifest.runtimeClass.caseValues.find(cv => cv.valueType.hasType(classOf[HttpResponse]))
  val outHttpResponseCaseValue = outManifest.runtimeClass.caseValues.find(cv => cv.valueType.hasType(classOf[HttpResponse]))

  def apply(request: In): Out

  override def onReceive(request: HttpRequest, response: HttpResponse) = {
    val json = requestJSON(request)
    val input = if (inManifest.runtimeClass == classOf[Unit]) {
      null.asInstanceOf[In]
    } else {
      var in = json match {
        case Some(content) if content != null && content.trim.nonEmpty => {
          typedJSON[In](content)
        }
        case _ => typedJSON[In]("{}")
      }
      in = inHttpRequestCaseValue match {
        case Some(cv) => cv.copy(in, request)
        case None => in
      }
      in = inHttpResponseCaseValue match {
        case Some(cv) => cv.copy(in, response)
        case None => in
      }
      in
    }

    try {
      val out = apply(input)
      outHttpResponseCaseValue match {
        case Some(cv) => cv[HttpResponse](out.asInstanceOf[AnyRef])
        case None => response.copy(content = StringContent(toJSON(out).stringify(pretty = pretty()), ContentType.JSON), status = HttpResponseStatus.OK)
      }
    } catch {
      case exc: ServiceException => {
        warn(s"ServiceException occurred: ${exc.message} (${exc.code}) with input of $input.", exc.cause)
        response.copy(content = StringContent(toJSON(exc.response).stringify(pretty = pretty()), ContentType.JSON), status = HttpResponseStatus.InternalServerError)
      }
      case t: Throwable => {
        error(s"Error occurred in handler function with input of $input.", t)
        response.copy(content = StringContent(Service.InternalError), status = HttpResponseStatus.InternalServerError)
      }
    }
  }

  private def requestJSON(request: HttpRequest): Option[String] = if (request.method != Method.Get && request.content.nonEmpty) {
    request.contentString
  } else if (request.url.parameters.values.nonEmpty) {
    if (inManifest.runtimeClass == classOf[String] && request.url.parameters.values.size == 1) {
      request.url.parameters.values.head._2.headOption.map(s => JString(s).stringify(pretty = pretty()))
    } else {
      val map = request.url.parameters.values.map {
        case (key, values) => if (values.tail.nonEmpty) {
          key -> values
        } else {
          key -> values.head
        }
      }
      val json = toJSON(map)
      Some(json.stringify(pretty = pretty()))
    }
  } else {
    None
  }

  def bindTo[S <: Session](application: WebApplication[S], uris: String*) = {
    uris.foreach {
      case uri => PathMappingHandler.add(application, uri, this)
    }
  }

  def unbindFrom[S <: Session](application: WebApplication[S], uris: String*) = {
    uris.foreach {
      case uri => PathMappingHandler.remove(application, uri)
    }
  }
}

object Service {
  MapSupport.o2j.excludeClass[ExceptionResponse]

  private val EmptyResponseMessage = "No content in request."
  private val InternalError = "An error occurred processing the service request."

  def apply[In, Out](f: In => Out)(implicit manifest: Manifest[In], outManifest: Manifest[Out]) = {
    new Service[In, Out] {
      override def apply(request: In) = f(request)
    }
  }
}