package com.outr.net.http.request

import java.util.Locale

import scala.util.Try

import org.powerscala.log.Logging

import com.outr.net.http.{Cookie, HttpApplication, HttpHeaders}

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpRequestHeaders(values: Map[String, String]) extends HttpHeaders with Logging {
  lazy val IfModifiedSince = date(HttpRequestHeaders.IfModifiedSince)
  lazy val AcceptEncoding = get(HttpRequestHeaders.AcceptEncoding)
  lazy val UserAgent = get(HttpRequestHeaders.UserAgent)

  def parseCookies() = {
    get("Cookie").map(s => s.split(";").map(parseCookie).toMap) match {
      case Some(c) => c
      case None => Map.empty[String, Cookie]
    }
  }

  private def parseCookie(s: String) = {
    val splitPoint = s.indexOf('=')
    if (splitPoint > 0) {
      val name = s.substring(0, splitPoint).trim
      val value = s.substring(splitPoint + 1).trim match {
        case v if v.startsWith("\"") && v.endsWith("\"") => unescape(v.substring(1, v.length - 1))
        case v => v
      }
      name -> Cookie(name = name, value = value)
    } else {
      s -> Cookie(name = s, value = "")
    }
  }

  private def unescape(s: String) = {   // TODO: make this work a lot better
    s.replaceAll("""\\"""", "\"")
  }

  def gzipSupport = AcceptEncoding match {
    case Some(encoding) if encoding != null => encoding.contains("gzip")
    case _ => false
  }

  def date(key: String) = values.get(key) match {
    case Some(value) if value.nonEmpty =>
      Try(
        Some(HttpApplication.DateParser.parse(value).getTime)
      ).getOrElse {
        warn(s"Unable to parse date from ($key): [value=$value]")
        None
      }

    case _ => None
  }

  def list(key: String) = get(key).map(s => s.split(',').map(_.trim).toList)
}

object HttpRequestHeaders {
  lazy val Empty = HttpRequestHeaders(Map.empty)

  val IfModifiedSince = "If-Modified-Since"
  val AcceptEncoding = "Accept-Encoding"
  val AcceptLanguage = "Accept-Language"
  val UserAgent = "User-Agent"
  val ForwardedFor = "X-Forwarded-For"
  val ForwardedForHost = "X-Forwarded-For-Host"
  val ForwardedForPort = "X-Forwarded-For-Port"
  val Authorization = "Authorization"
}
