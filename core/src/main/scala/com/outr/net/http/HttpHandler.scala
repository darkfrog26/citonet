package com.outr.net.http

import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpHandler {
  def onReceive(request: HttpRequest, response: HttpResponse): HttpResponse
}

object HttpHandler {
  def apply(f: HttpRequest => HttpResponse) = new HttpHandler {
    def onReceive(request: HttpRequest, response: HttpResponse) = try {
      response.merge(f(request))
    } catch {
      case t: Throwable => throw new RuntimeException(s"Failed to merge for URL: ${request.url}", t)
    }
  }
}