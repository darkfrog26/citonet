package com.outr.net.http.handler

import com.outr.net.http.HttpApplication
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HandlerApplication extends HttpApplication {
  val handlers = new HandlerProcessor()

  override protected def processRequest(request: HttpRequest, response: HttpResponse) = {
    handlers.fire(request -> response)
  }
}
