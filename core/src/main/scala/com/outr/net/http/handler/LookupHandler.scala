package com.outr.net.http.handler

import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import com.outr.net.URL
import com.outr.net.http.content.HttpContent
import com.outr.net.http.HttpHandler

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait LookupHandler extends HttpHandler {
  /**
   * Receives the decoded URL for lookup.
   *
   * @param url decoded URL
   * @return Option[HttpContent]
   */
  def lookup(url: URL): Option[HttpContent]

  def onReceive(request: HttpRequest, response: HttpResponse) = if (response.status == HttpResponseStatus.NotFound) {
    lookup(request.url.decoded) match {
      case Some(content) => response.copy(content = content, status = HttpResponseStatus.OK)
      case None => response
    }
  } else {
    response
  }
}
