package com.outr.net.proxy

import com.outr.net.http.request.HttpRequest

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HostProxy(originHost: String, destinationHost: String, destinationPort: Option[Int] = None, rewriteURL: Boolean = false) extends Proxy {
  def get(request: HttpRequest) = if (request.url.host.equalsIgnoreCase(originHost)) {
    val port = destinationPort match {
      case Some(p) => p
      case None => request.url.port
    }
    val url = request.url.copy(host = destinationHost, port = port)
    val host = if (rewriteURL) {
      s"$destinationHost:$port"
    } else {
      s"$originHost:${request.url.port}"
    }
    Some(request.copy(url = url).header("Host", host))
  } else {
    None
  }
}