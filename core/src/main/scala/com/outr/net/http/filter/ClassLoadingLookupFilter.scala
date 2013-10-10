package com.outr.net.http.filter

import com.outr.net.URL
import com.outr.net.http.content.{URLContent, HttpContent}

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class ClassLoadingLookupFilter(urlBasePath: String,
                                    lookupPath: String,
                                    allowCaching: Boolean,
                                    priority: Double = HttpFilter.Low) extends LookupFilter {
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
