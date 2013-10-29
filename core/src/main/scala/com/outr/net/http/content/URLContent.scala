package com.outr.net.http.content

import com.outr.net.URL
import com.outr.net.http.mime.MimeType

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class URLContent(url: URL,
                      contentTypeOverride: ContentType = null,
                      allowCaching: Boolean = true) extends StreamableContent {
  if (url == null) {
    throw new NullPointerException("URL cannot be null!")
  }

  lazy val connection = url.javaURL.openConnection()

  lazy val input = connection.getInputStream

  lazy val contentType = if (contentTypeOverride != null) {
    contentTypeOverride
  } else {
    ContentType.parse(MimeType.lookup(url.extension, connection.getContentType))
  }

  lazy val contentLength = connection.getContentLengthLong

  lazy val lastModified = if (allowCaching) connection.getLastModified else -1L

  override def toString = s"URLContent($url, contentType: $contentType, contentLength: $contentLength)"
}