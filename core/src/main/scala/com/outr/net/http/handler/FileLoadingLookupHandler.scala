package com.outr.net.http.handler

import com.outr.net.URL
import com.outr.net.http.content.{FileContent, HttpContent}
import java.io.File

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class FileLoadingLookupHandler(urlBasePath: String,
                                    directory: File,
                                    allowCaching: Boolean) extends LookupHandler {
  def lookup(url: URL): Option[HttpContent] = if (url.path.startsWith(urlBasePath)) {
    val path = url.path.substring(urlBasePath.length)
    val file = new File(directory, path)
    if (file.isFile) {
      Some(FileContent(file, allowCaching = allowCaching))
    } else {
      None
    }
  } else {
    None
  }
}
