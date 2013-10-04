package com.outr.citonet.http.filter

import com.outr.citonet.URL
import com.outr.citonet.http.response.{URLResponseContent, ResponseContent}

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class ClassLoadingLookupFilter(urlBasePath: String, lookupPath: String, allowCaching: Boolean) extends LookupFilter {
  def lookup(url: URL): Option[ResponseContent] = if (url.path.startsWith(urlBasePath)) {
    val path = url.path.substring(urlBasePath.length)
    Thread.currentThread().getContextClassLoader.getResource(s"$lookupPath$path") match {
      case null => None
      case javaURL => {
        val url = URL(javaURL)
        Some(URLResponseContent(url, "text/html", allowCaching = allowCaching))
      }
    }
  } else {
    None
  }
}
