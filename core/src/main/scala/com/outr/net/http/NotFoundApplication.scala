package com.outr.net.http

import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import com.outr.net.http.content.HttpContent

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait NotFoundApplication extends HttpApplication {
  protected def notFoundContent(request: HttpRequest): HttpContent

  override def onReceive(request: HttpRequest, response: HttpResponse) = {
    val updated = super.onReceive(request, response)
    updated.status match {
      case HttpResponseStatus.NotFound if updated.content == null => response.copy(content = notFoundContent(request))
      case _ => updated
    }
  }
}
