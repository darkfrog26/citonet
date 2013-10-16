package com.outr.net.http.handler

import com.outr.net.URL
import com.outr.net.http.content.{URLContent, HttpContent}
import com.outr.net.http.HttpHandler

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class ClassLoadingLookupHandler(urlBasePath: String,
                                    lookupPath: String,
                                    allowCaching: Boolean,
                                    priority: Double = HttpHandler.Low) extends LookupHandler {
  def lookup(url: URL): Option[HttpContent] = if (url.path.startsWith(urlBasePath)) {
    val path = url.path.substring(urlBasePath.length)
    Thread.currentThread().getContextClassLoader.getResource(s"$lookupPath$path") match {
      case null => None
      case javaURL => {
        val url = URL(javaURL)
        Some(URLContent(url, allowCaching = allowCaching))
      }
    }
  } else {
    None
  }
}
