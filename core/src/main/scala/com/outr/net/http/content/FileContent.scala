package com.outr.net.http.content

import java.io.{FileInputStream, File}
import java.nio.file.Files
import com.outr.net.http.mime.MimeType

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
    val name = file.getName
    if (name.indexOf('.') != -1) {
      val extension = name.substring(name.lastIndexOf('.') + 1)
      ContentType.parse(MimeType.lookup(extension, Files.probeContentType(file.toPath)))
    } else {
      ContentType.parse(Files.probeContentType(file.toPath))
    }
  }
  lazy val contentLength = file.length()
  lazy val lastModified = if (allowCaching) file.lastModified() else -1L

  override def toString = s"FileContent(${file.getAbsolutePath}, contentType: $contentType, contentLength: $contentLength)"
}