package com.outr.net.http.content

import com.outr.net.URL

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class URLContent(url: URL,
                      contentTypeOverride: String = null,
                      allowCaching: Boolean = true) extends StreamableContent {
  lazy val connection = url.javaURL.openConnection()

  lazy val input = connection.getInputStream

  lazy val contentType = if (contentTypeOverride != null) {
    contentTypeOverride
  } else {
    connection.getContentType
  }

  lazy val contentLength = connection.getContentLengthLong

  lazy val lastModified = if (allowCaching) connection.getLastModified else -1L
}