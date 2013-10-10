package com.outr.net.http

import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import com.outr.net.http.content.HttpContent

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait NotFoundApplication extends HttpApplication {
  protected def notFoundContent(request: HttpRequest): HttpContent

  def onReceive(request: HttpRequest) = {
    HttpResponse(notFoundContent(request), HttpResponseStatus.NotFound)
  }
}
