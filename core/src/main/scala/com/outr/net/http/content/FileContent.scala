package com.outr.net.http.content

import java.io.{FileInputStream, File}
import java.nio.file.Files

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class FileContent(file: File,
                       contentTypeOverride: ContentType = null,
                       allowCaching: Boolean = true) extends StreamableContent {
  lazy val input = new FileInputStream(file)
  lazy val contentType = if (contentTypeOverride != null) {
    contentTypeOverride
  } else {
    // TODO: use MimeType instead of Files.probeContentType
    ContentType.parse(Files.probeContentType(file.toPath))
  }
  lazy val contentLength = file.length()
  lazy val lastModified = if (allowCaching) file.lastModified() else -1L

  override def toString = s"FileContent(${file.getAbsolutePath}, contentType: $contentType, contentLength: $contentLength)"
}