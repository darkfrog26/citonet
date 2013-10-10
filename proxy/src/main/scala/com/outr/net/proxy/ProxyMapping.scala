package com.outr.net.proxy

import com.outr.net.URL


/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyMapping {
  private var hostMapping = Map.empty[String, URL]
  private var hostPortMapping = Map.empty[String, URL]
  private var domainMapping = Map.empty[String, URL]

  def get(url: URL): Option[URL] = {
    val hostPortProxy = hostPortMapping.get(url.hostPort)
    if (hostPortProxy.isEmpty) {
      val hostProxy = hostMapping.get(url.host)
      if (hostProxy.isEmpty) {
        val domainProxy = domainMapping.get(url.domain)
        domainProxy
      } else {
        hostProxy
      }
    } else {
      hostPortProxy
    }
  }

  def host(hostname: String, proxy: URL) = synchronized {
    hostMapping += hostname -> proxy
  }

  def hostPort(hostname: String, port: Int, proxy: URL) = synchronized {
    hostPortMapping += s"$hostname:$port" -> proxy
  }

  def domain(domain: String, proxy: URL) = synchronized {
    domainMapping += domain -> proxy
  }
}