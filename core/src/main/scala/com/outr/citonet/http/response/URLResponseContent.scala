package com.outr.citonet.http.response

import com.outr.citonet.URL

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class URLResponseContent(url: URL, contentTypeOverride: String = null) extends StreamableResponseContent {
  lazy val connection = url.javaURL.openConnection()

  lazy val input = connection.getInputStream

  lazy val contentType = if (contentTypeOverride != null) {
    contentTypeOverride
  } else {
    connection.getContentType
  }

  lazy val contentLength = connection.getContentLengthLong

  lazy val lastModified = connection.getLastModified
}
