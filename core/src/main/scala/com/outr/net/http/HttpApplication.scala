package com.outr.net.http

import org.powerscala.property.{ListProperty, Property}
import com.outr.net.HasHostAndPort
import org.powerscala.event.Listenable
import java.text.SimpleDateFormat
import com.outr.net.http.response.{HttpResponse, HttpResponseStatus}
import com.outr.net.http.request.HttpRequest

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpApplication extends Listenable with HttpHandler {
  val bindings = new Property[List[HasHostAndPort]](default = Some(Nil)) with ListProperty[HasHostAndPort]

  def priority = HttpHandler.Normal

  def init(): Unit

  protected def processRequest(request: HttpRequest, response: HttpResponse) = {
    onReceive(request, response)
  }

  def receive(request: HttpRequest) = {
    val response = processRequest(request, HttpResponse(status = HttpResponseStatus.NotFound))
    val cached = request.headers.ifModifiedSince match {
      case Some(modified) if response.content.lastModified != -1 => modified >= response.content.lastModified
      case _ => false
    }
    if (cached) {
      response.copy(status = HttpResponseStatus.NotModified, content = null)
    } else {
      response
    }
  }
}

object HttpApplication {
  val DateParser = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
}