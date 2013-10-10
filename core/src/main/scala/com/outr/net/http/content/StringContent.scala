package com.outr.net.http.content

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class StringContent(value: String,
                         contentType: String = "text/plain",
                         lastModified: Long = -1L) extends HttpContent {
  lazy val contentLength = value.length.toLong
}