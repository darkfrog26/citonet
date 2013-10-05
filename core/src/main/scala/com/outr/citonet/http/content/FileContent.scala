package com.outr.citonet.http.content

import java.io.{FileInputStream, File}
import java.nio.file.Files

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class FileContent(file: File,
                       contentTypeOverride: String = null,
                       allowCaching: Boolean = true) extends StreamableContent {
  lazy val input = new FileInputStream(file)
  lazy val contentType = if (contentTypeOverride != null) {
    contentTypeOverride
  } else {
    Files.probeContentType(file.toPath)
  }
  lazy val contentLength = file.length()
  lazy val lastModified = if (allowCaching) file.lastModified() else -1L
}