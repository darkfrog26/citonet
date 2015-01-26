package com.outr.net.service

import com.outr.net.http.content.{ContentType, StringContent}
import com.outr.net.http.handler.PathMappingHandler
import com.outr.net.http.session.Session
import com.outr.net.http.{WebApplication, HttpHandler}
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}

import org.powerscala.json._
import org.powerscala.log.Logging
import org.powerscala.property.Property

/**
 * @author Matt Hicks <matt@outr.com>
 */
abstract class Service[In <: AnyRef, Out](implicit inManifest: Manifest[In], outManifest: Manifest[Out]) extends HttpHandler with Logging {
  val pretty = Property[Boolean](default = Some(false))

  def apply(request: In): Out

  override def onReceive(request: HttpRequest, response: HttpResponse) = {
    val json = requestJSON(request)
    json match {
      case Some(content) => {
        val in = typedJSON[In](content)
        try {
          val out = apply(in.asInstanceOf[In])
          response.copy(content = StringContent(toJSON(out).stringify(pretty = pretty()), ContentType.JSON), status = HttpResponseStatus.OK)
        } catch {
          case t: Throwable => {
            warn(s"Error occurred in handler function with input of $in.", t)
            response.copy(content = StringContent(Service.InternalError), status = HttpResponseStatus.InternalServerError)
          }
        }
      }
      case None => response.copy(content = StringContent(Service.EmptyResponseMessage), status = HttpResponseStatus.BadRequest)
    }
  }

  private def requestJSON(request: HttpRequest): Option[String] = if (request.content.nonEmpty) {
    request.contentString
  } else if (request.url.parameters.values.nonEmpty) {
    val map = request.url.parameters.values.map {
      case (key, values) => if (values.tail.nonEmpty) {
        key -> values
      } else {
        key -> values.head
      }
    }
    val json = toJSON(map)
    Some(json.stringify(pretty = pretty()))
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
  private val EmptyResponseMessage = "No content in request."
  private val InternalError = "An error occurred processing the service request."

  def apply[In <: AnyRef, Out](f: In => Out)(implicit manifest: Manifest[In], outManifest: Manifest[Out]) = {
    new Service[In, Out] {
      override def apply(request: In) = f(request)
    }
  }
}