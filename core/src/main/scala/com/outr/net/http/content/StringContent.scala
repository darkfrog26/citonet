package com.outr.net.http.content

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class StringContent(value: String,
                         contentType: ContentType = ContentType.Plain,
                         lastModified: Long = -1L) extends HttpContent {
  lazy val contentLength = value.length.toLong

  override def asString = value

  override def toString = s"StringContent($value, contentType: $contentType, contentLength: $contentLength)"
}