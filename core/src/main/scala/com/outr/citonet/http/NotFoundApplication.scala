package com.outr.citonet.http

import com.outr.citonet.http.request.HttpRequest
import com.outr.citonet.http.response.{HttpResponseStatus, HttpResponse}
import com.outr.citonet.http.content.HttpContent

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait NotFoundApplication extends HttpApplication {
  protected def notFoundContent(request: HttpRequest): HttpContent

  def onReceive(request: HttpRequest) = {
    HttpResponse(notFoundContent(request), HttpResponseStatus.NotFound)
  }
}
