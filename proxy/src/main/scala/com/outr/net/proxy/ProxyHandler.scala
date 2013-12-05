package com.outr.net.proxy

import com.outr.net.http.handler.HandlerListener
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse
import org.powerscala.Priority
import com.outr.net.http.client.HttpClient

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class ProxyHandler(proxy: Proxy, priority: Priority = Priority.Normal) extends HandlerListener {
  def onReceive(request: HttpRequest, response: HttpResponse) = proxy.get(request) match {
    case Some(proxyRequest) => HttpClient.send(proxyRequest)
    case None => response
  }
}