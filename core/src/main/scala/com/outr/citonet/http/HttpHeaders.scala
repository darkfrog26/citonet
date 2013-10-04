package com.outr.citonet.http

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpHeaders {
  def values: Map[String, String]
}

case class HttpRequestHeaders(values: Map[String, String] = Map.empty) extends HttpHeaders {
  lazy val ifModifiedSince = date("If-Modified-Since")

  def date(key: String) = values.get(key).map(HttpApplication.DateParser.parse).map(d => d.getTime)
}

case class HttpResponseHeaders(values: Map[String, String] = Map.empty) extends HttpHeaders {
}