package com.outr.net.http.request

import com.outr.net.{IP, Method, URL}
import com.outr.net.http.Cookie
import com.outr.net.http.content.{InputStreamContent, HttpContent}
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

  /**
   * Allows storage and retrieval of temporary values exclusive to this request.
   */
  lazy val store = new MapStorage[String, Any]

  lazy val contentString = content match {
    case Some(c) => c match {
      case isc: InputStreamContent => Some(IO.copy(isc.input))
    }
    case None => None
  }

  override def toString = s"$url ($method)"
}