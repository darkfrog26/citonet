package com.outr.citonet.http

import com.outr.citonet.http.request.HttpRequest
import com.outr.citonet.http.response.HttpResponse

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