package com.outr.net.service

import com.outr.net.http.content.{ContentType, StringContent}
import com.outr.net.http.session.Session
import com.outr.net.http.{WebApplication, HttpHandler}
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}

import org.powerscala.json._
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
class Service[In, Out] private(f: In => Out)(implicit inManifest: Manifest[In], outManifest: Manifest[Out]) extends HttpHandler with Logging {
  override def onReceive(request: HttpRequest, response: HttpResponse) = {
    val json = requestJSON(request)
    json match {
      case Some(content) => {
        val in = fromJSON(content)
        if (!in.getClass.isAssignableFrom(inManifest.runtimeClass)) {
          response.copy(content = StringContent(s"Unable to convert $content to ${inManifest.runtimeClass.getName}. Converted status: $in"), status = HttpResponseStatus.BadRequest)
        } else {
          try {
            val out = f(in.asInstanceOf[In])
            response.copy(content = StringContent(toJSON(out).pretty, ContentType.JSON), status = HttpResponseStatus.OK)
          } catch {
            case t: Throwable => {
              warn(s"Error occurred in handler function with input of $in.", t)
              response.copy(content = StringContent(Service.InternalError), status = HttpResponseStatus.InternalServerError)
            }
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
    Some(toJSON(map).pretty)
  } else {
    None
  }

  def bindTo[S <: Session](application: WebApplication[S], uris: String*) = {
    application.addHandler(this, uris: _*)
  }
}

object Service {
  private val EmptyResponseMessage = "No content in request."
  private val InternalError = "An error occurred processing the service request."

  def apply[In, Out](f: In => Out)(implicit manifest: Manifest[In], outManifest: Manifest[Out]) = {
    new Service[In, Out](f)
  }
}