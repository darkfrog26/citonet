package com.outr.citonet.http.response

import java.io.{FileInputStream, File}
import java.nio.file.Files

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class FileResponseContent(file: File, contentTypeOverride: String = null) extends StreamableResponseContent {
  lazy val input = new FileInputStream(file)
  lazy val contentType = if (contentTypeOverride != null) {
    contentTypeOverride
  } else {
    Files.probeContentType(file.toPath)
  }
  lazy val contentLength = file.length()
  lazy val lastModified = file.lastModified()
}