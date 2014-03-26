package com.outr.net.http.handler

import com.outr.net.http.HttpApplication
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HandlerApplication extends HttpApplication with Logging {
  val handlers = new HandlerProcessor()

  override def onReceive(request: HttpRequest, response: HttpResponse) = contextualize(request) {
    val updated = handlers.fire(request -> response)
    super.onReceive(request, updated)
  }
}