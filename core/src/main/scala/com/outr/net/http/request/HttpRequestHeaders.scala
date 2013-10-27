package com.outr.net.http.request

import com.outr.net.http.{HttpApplication, HttpHeaders}
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpRequestHeaders(values: Map[String, String]) extends HttpHeaders with Logging {
  lazy val IfModifiedSince = date("If-Modified-Since")
  lazy val AcceptEncoding = values.get("Accept-Encoding")
  lazy val UserAgent = values.get("User-Agent")

  def gzipSupport = AcceptEncoding match {
    case Some(encoding) if encoding != null => encoding.contains("gzip")
    case _ => false
  }

  def date(key: String) = values.get(key) match {
    case Some(value) if value.nonEmpty => try {
      Some(HttpApplication.DateParser.parse(value).getTime)
    } catch {
      case exc: NumberFormatException => {
        warn(s"Unable to parse date from ($key): [$value]", exc)
        None
      }
    }
    case _ => None
  }
}

object HttpRequestHeaders {
  lazy val Empty = HttpRequestHeaders(Map.empty)
}