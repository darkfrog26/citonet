package com.outr.citonet.http

import org.powerscala.property.{ListProperty, Property}
import com.outr.citonet.HasHostAndPort
import org.powerscala.event.Listenable
import java.text.SimpleDateFormat
import com.outr.citonet.http.response.{HttpResponseStatus, HttpResponse}
import com.outr.citonet.http.request.HttpRequest

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpApplication extends Listenable {
  val bindings = new Property[List[HasHostAndPort]](default = Some(Nil)) with ListProperty[HasHostAndPort]

  protected def onReceive(request: HttpRequest): HttpResponse

  final def receive(request: HttpRequest) = {
    val response = onReceive(request)
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