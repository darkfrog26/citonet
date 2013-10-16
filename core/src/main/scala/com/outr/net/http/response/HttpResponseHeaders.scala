package com.outr.net.http.response

import com.outr.net.http.HttpHeaders

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpResponseHeaders(values: Map[String, String] = Map.empty) extends HttpHeaders {
  def merge(headers: HttpHeaders) = {
    HttpResponseHeaders(values ++ headers.values)
  }
}