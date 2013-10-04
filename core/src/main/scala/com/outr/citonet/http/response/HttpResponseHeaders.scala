package com.outr.citonet.http.response

import com.outr.citonet.http.HttpHeaders

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpResponseHeaders(values: Map[String, String] = Map.empty) extends HttpHeaders {
}