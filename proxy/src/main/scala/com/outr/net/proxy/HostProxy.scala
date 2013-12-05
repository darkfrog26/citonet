package com.outr.net.proxy

import com.outr.net.http.request.HttpRequest

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HostProxy(originHost: String, destinationHost: String, destinationPort: Option[Int] = None) extends Proxy {
  def get(request: HttpRequest) = if (request.url.host.equalsIgnoreCase(originHost)) {
    val port = destinationPort match {
      case Some(p) => p
      case None => request.url.port
    }
    Some(request.copy(request.url.copy(host = destinationHost, port = port)))
  } else {
    None
  }
}