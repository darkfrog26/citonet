package com.outr.net.proxy

import com.outr.net.http.handler.HandlerListener
import com.outr.net.http.request.{HttpRequestHeaders, HttpRequest}
import com.outr.net.http.response.HttpResponse
import org.powerscala.Priority
import com.outr.net.http.client.HttpClient
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class ProxyHandler(proxy: Proxy, priority: Priority = Priority.Normal) extends HandlerListener with Logging {
  def onReceive(request: HttpRequest, response: HttpResponse) = proxy.get(request) match {
    case Some(proxyRequest) => {
      info(s"Proxying: ${request.url} to ${proxyRequest.url}")
      HttpClient.send(ProxyHandler.forwarding(proxyRequest))
    }
    case None => response
  }
}

object ProxyHandler {
  /**
   * Modify the HttpRequest before forwarding to a remote server. This means configuring proxying headers mostly.
   *
   * @param request the request being forwarded
   * @return modified HttpRequest
   */
  def forwarding(request: HttpRequest) = {
    val forwardedFor = (request.headers.list(HttpRequestHeaders.ForwardedFor).getOrElse(Nil) ::: List(request.remoteAddress.toString)).mkString(", ")
    val forwardedForHost = (request.headers.list(HttpRequestHeaders.ForwardedForHost).getOrElse(Nil) ::: List(request.remoteHost)).mkString(", ")
    val forwardedForPort = (request.headers.list(HttpRequestHeaders.ForwardedForPort).getOrElse(Nil) ::: List(request.remotePort.toString)).mkString(", ")
    request
      .header(HttpRequestHeaders.ForwardedFor, forwardedFor)
      .header(HttpRequestHeaders.ForwardedForHost, forwardedForHost)
      .header(HttpRequestHeaders.ForwardedForPort, forwardedForPort)
  }
}