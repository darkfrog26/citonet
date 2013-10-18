package com.outr.net.http.response

import com.outr.net.http.HttpHeaders

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpResponseHeaders(values: Map[String, String] = Map.empty) extends HttpHeaders {
  def CacheControl(value: String = "no-cache, max-age=0, must-revalidate, no-store") = {
    copy(values + ("Cache-Control" -> value))
  }

  def merge(headers: HttpHeaders) = {
    HttpResponseHeaders(values ++ headers.values)
  }
}