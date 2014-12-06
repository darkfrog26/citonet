package com.outr.net.http.filter

import com.outr.net.http.{HttpApplication, HttpHandler}
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse

/**
 * Allows specifying a base-path to direct traffic to another handler.
 *
 * @author Matt Hicks <matt@outr.com>
 */
case class PathFilter(path: String, handler: HttpHandler, removePath: Boolean = true) extends HandlerFilter {
  override def accept(request: HttpRequest, response: HttpResponse) = request.url.path.toLowerCase.startsWith(path.toLowerCase)

  override def modify(request: HttpRequest) = if (removePath) {
    val url = request.url.copy(path = request.url.path.substring(path.length))
    request.copy(url = url)
  } else {
    request
  }

  override def handle(request: HttpRequest, response: HttpResponse) = handler match {
    case application: HttpApplication => HttpApplication.around(request) {
      handler.onReceive(request, response)
    }
    case _ => handler.onReceive(request, response)
  }
}
