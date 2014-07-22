package com.outr.net.http.request

import com.outr.net.{IP, Method, URL}
import com.outr.net.http.Cookie
import com.outr.net.http.content.{FormPostContent, InputStreamContent, HttpContent}
import org.powerscala.{MapStorage, IO}

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpRequest(url: URL,
                       method: Method = Method.Get,
                       headers: HttpRequestHeaders = HttpRequestHeaders.Empty,
                       cookies: Map[String, Cookie] = Map.empty,
                       content: Option[HttpContent] = None,
                       remoteAddress: IP = IP.LocalHost,
                       remoteHost: String = "localhost",
                       remotePort: Int = -1) {
  def cookie(name: String) = cookies.get(name)
  def header(key: String, value: String) = copy(headers = headers.copy(values = headers.values + (key -> value)))

  /**
   * Allows storage and retrieval of temporary values exclusive to this request.
   */
  lazy val store = new MapStorage[String, Any]

  def contentString = content.map(c => c.asString)

  /**
   * The originating remoteAddress specified by proxy or the current value of the remoteAddress.
   */
  lazy val derivedRemoteAddress = headers.list(HttpRequestHeaders.ForwardedFor) match {
    case Some(list) => IP(list.head)
    case None => remoteAddress
  }

  /**
   * The originating remoteHost specified by proxy or the current value of the remoteHost.
   */
  lazy val derivedRemoteHost = headers.list(HttpRequestHeaders.ForwardedForHost) match {
    case Some(list) => list.head
    case None => remoteHost
  }

  /**
   * The originating remotePort specified by proxy or the current value of the remotePort.
   */
  lazy val derivedRemotePort = headers.list(HttpRequestHeaders.ForwardedForPort) match {
    case Some(list) => list.head.toInt
    case None => remotePort
  }

  override def toString = s"$url ($method)"
}