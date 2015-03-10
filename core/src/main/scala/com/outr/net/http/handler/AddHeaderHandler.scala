package com.outr.net.http.handler

import com.outr.net.http.HttpHandler
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse

/**
 * @author Matt Hicks <matt@outr.com>
 */
class AddHeaderHandler(key: String, value: String) extends HttpHandler {
  override def onReceive(request: HttpRequest, response: HttpResponse) = response.setHeader(key, value)
}
