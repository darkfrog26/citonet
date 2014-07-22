package com.outr.net.http.content

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpContent {
  def contentType: ContentType
  def contentLength: Long
  def lastModified: Long

  def asString: String = throw new UnsupportedOperationException(s"$getClass doesn't support HttpContent.asString")
}
