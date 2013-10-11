package com.outr.net.communicator.server

import com.outr.net.http.HttpHandler
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse
import com.outr.net.http.content.StringContent

/**
 * @author Matt Hicks <matt@outr.com>
 */
object CommunicatorHandler extends HttpHandler {
  def onReceive(request: HttpRequest) = {
    println(s"Communication received: $request")
    HttpResponse(StringContent("Communicator handler!"))
  }
}