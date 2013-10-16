package com.outr.net.http.handler

import com.outr.net.http.{HttpHandler, HttpApplication}
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse
import scala.annotation.tailrec

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HandlerApplication extends HttpApplication {
  private var _handlers = List.empty[HttpHandler]
  def handlers = _handlers

  override protected def processRequest(request: HttpRequest, response: HttpResponse) = {
    processHandlers(request, response, handlers)
  }

  @tailrec
  private def processHandlers(request: HttpRequest, response: HttpResponse, handlers: List[HttpHandler]): HttpResponse = {
    if (handlers.isEmpty) {
      response
    } else {
      val handler = handlers.head
      processHandlers(request, handler.onReceive(request, response), handlers.tail)
    }
  }

  def addHandler[H <: HttpHandler](handler: H): H = synchronized {
    _handlers = (handler :: handlers).sortBy(h => h.priority).reverse
    handler
  }

  def removeHandler[H <: HttpHandler](handler: H): H = synchronized {
    _handlers = _handlers.filterNot(h => h == handler)
    handler
  }
}
