package com.outr.net.http

import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpHandler {
  def onReceive(request: HttpRequest): HttpResponse
}

object HttpHandler {
  def apply(f: HttpRequest => HttpResponse) = new HttpHandler {
    def onReceive(request: HttpRequest) = f(request)
  }
}