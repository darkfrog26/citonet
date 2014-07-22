package com.outr.net.http.content

import com.outr.net.http.HttpParameters

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class FormPostContent(contentString: String) extends HttpContent {
  val parameters = HttpParameters.parse(contentString)

  override def lastModified = -1L

  override def contentLength = contentString.length

  override def contentType = ContentType.FormURLEncoded

  override lazy val asString = contentString
}
