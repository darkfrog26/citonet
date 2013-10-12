package com.outr.net.http.request

import com.outr.net.{Method, URL}
import com.outr.net.http.Cookie
import com.outr.net.http.content.HttpContent

/**
 * @author Matt Hicks <matt@outr.com>
 */
class HttpRequest(val url: URL,
                  val method: Method,
                  val headers: HttpRequestHeaders,
                  val cookies: Map[String, Cookie],
                  val content: Option[HttpContent]) {
  def cookie(name: String) = cookies.get(name)

  override def toString = s"$url ($method)"
}

object HttpRequest {
  def apply(url: URL,
            method: Method = Method.Get,
            headers: HttpRequestHeaders = HttpRequestHeaders.Empty,
            cookies: Map[String, Cookie] = null,
            content: Option[HttpContent] = None): HttpRequest = {
    val _cookies = if (cookies != null) {
      cookies
    } else {
      headers.values.get("Cookie").map(s => s.split(";").map(parseCookie).toMap) match {
        case Some(c) => c
        case None => Map.empty[String, Cookie]
      }
    }
    new HttpRequest(url, method, headers, _cookies, content)
  }

  private def parseCookie(s: String) = {
    val splitPoint = s.indexOf('=')
    val name = s.substring(0, splitPoint).trim
    val value = s.substring(splitPoint + 1).trim match {
      case v if v.startsWith("\"") && v.endsWith("\"") => unescape(v.substring(1, v.length - 1))
      case v => v
    }
    name -> Cookie(name = name, value = value)
  }

  private def unescape(s: String) = {   // TODO: make this work a lot better
    s.replaceAll("""\\"""", "\"")
  }
}