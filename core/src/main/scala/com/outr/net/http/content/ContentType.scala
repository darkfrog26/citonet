package com.outr.net.http.content

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ContentType(val value: String) {
  override def toString = value

  def isMultipart = false
}

object ContentType {
  lazy val Plain = new ContentType("text/plain")
  lazy val HTML = new ContentType("text/html")
  lazy val JSON = new ContentType("application/json")

  def parse(contentType: String) = if (contentType == null) {
    null
  } else if (contentType.startsWith("multipart/form-data;")) {
    val boundary = contentType.substring(contentType.indexOf("boundary=") + 9).trim
    new MultipartContentType(boundary)
  } else {
    new ContentType(contentType)
  }
}