package com.outr.net.http.request

import com.outr.net.URL
import com.outr.net.http.Cookie

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpRequest(url: URL, headers: HttpRequestHeaders, cookies: Map[String, Cookie]) {
  def cookie(name: String) = cookies.get(name)
}

object HttpRequest {
  def apply(url: URL, headers: HttpRequestHeaders): HttpRequest = {
    val cookies = headers.values.get("Cookie").map(s => s.split(";").map(parseCookie).toMap) match {
      case Some(c) => c
      case None => Map.empty[String, Cookie]
    }
    apply(url, headers, cookies)
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