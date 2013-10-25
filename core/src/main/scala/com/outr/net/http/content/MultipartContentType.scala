package com.outr.net.http.content

/**
 * @author Matt Hicks <matt@outr.com>
 */
class MultipartContentType(val boundary: String) extends ContentType("multipart/form-data") {
  override def isMultipart = true

  override def toString = s"$value; boundary=$boundary"
}