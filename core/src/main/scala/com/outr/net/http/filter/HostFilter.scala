package com.outr.net.http.filter

import com.outr.net.http.{HttpApplication, HttpHandler}
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse

/**
 * Allows mapping of a specific host to the wrapped handler.
 *
 * @author Matt Hicks <matt@outr.com>
 */
case class HostFilter(host: String, handler: HttpHandler) extends HandlerFilter {
  override def accept(request: HttpRequest, response: HttpResponse) = request.url.host.equalsIgnoreCase(host)

  override def modify(request: HttpRequest) = request

  override def handle(request: HttpRequest, response: HttpResponse) = handler match {
    case application: HttpApplication => application.contextualize(request) {
      handler.onReceive(request, response)
    }
    case _ => handler.onReceive(request, response)
  }
}
