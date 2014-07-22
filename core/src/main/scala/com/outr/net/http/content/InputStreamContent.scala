package com.outr.net.http.content

import java.io.InputStream

import org.powerscala.IO

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class InputStreamContent(input: InputStream,
                              contentType: ContentType,
                              contentLength: Long,
                              lastModified: Long) extends StreamableContent {
  override lazy val asString = IO.copy(input)
}