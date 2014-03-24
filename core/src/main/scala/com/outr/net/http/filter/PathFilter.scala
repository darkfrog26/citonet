package com.outr.net.http.filter

import com.outr.net.http.HttpHandler
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse
import com.outr.net.http.handler.HandlerApplication

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
    case application: HandlerApplication => application.around {
      application.receiveContextualized(request) {
        case r => r
      }
    }
    case _ => handler.onReceive(request, response)
  }
}
