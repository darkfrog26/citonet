package com.outr.net.http.request

import com.outr.net.http.{HttpApplication, HttpHeaders}

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpRequestHeaders(values: Map[String, String]) extends HttpHeaders {
  lazy val ifModifiedSince = date("If-Modified-Since")
  lazy val acceptEncoding = values.get("Accept-Encoding")

  def gzipSupport = acceptEncoding match {
    case Some(encoding) => encoding.contains("gzip")
    case None => false
  }

  def date(key: String) = values.get(key).map(HttpApplication.DateParser.parse).map(d => d.getTime)
}

object HttpRequestHeaders {
  lazy val Empty = HttpRequestHeaders(Map.empty)
}