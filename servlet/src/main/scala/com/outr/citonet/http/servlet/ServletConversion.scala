package com.outr.citonet.http.servlet

import com.outr.citonet.{ArrayBufferPool, URL}
import scala.collection.JavaConversions._
import org.powerscala.IO
import com.outr.citonet.http.response.{StreamableResponseContent, HttpResponse}
import com.outr.citonet.http.request.{HttpRequestHeaders, HttpRequest}
import javax.servlet.http

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ServletConversion {
  def convert(servletRequest: javax.servlet.http.HttpServletRequest) = {
    val requestURL = servletRequest.getRequestURL.toString
    val url = URL.parse(requestURL).getOrElse(throw new NullPointerException(s"Unable to parse: [$requestURL]"))
    val headers = servletRequest.getHeaderNames.map(name => name -> servletRequest.getHeader(name)).toMap
    HttpRequest(url, HttpRequestHeaders(headers))
  }

  def convert(response: HttpResponse, servletResponse: javax.servlet.http.HttpServletResponse, gzip: Boolean) = {
    servletResponse.setStatus(response.status.code)
    response.headers.values.foreach {
      case (key, value) => servletResponse.setHeader(key, value)
    }
    response.cookies.foreach {
      case cookie => {
        val servletCookie = new http.Cookie(cookie.name, cookie.value)
        if (cookie.comment != null) servletCookie.setComment(cookie.comment)
        if (cookie.domain != null) servletCookie.setDomain(cookie.domain)
        servletCookie.setHttpOnly(cookie.httpOnly)
        if (cookie.maxAge != Int.MinValue) servletCookie.setMaxAge(cookie.maxAge)
        if (cookie.path != null) servletCookie.setPath(cookie.path)
        servletCookie.setSecure(cookie.secure)
        servletCookie.setVersion(cookie.version)
        servletResponse.addCookie(servletCookie)
      }
    }
    if (response.content != null) {
      servletResponse.setContentType(response.content.contentType)
      if (response.content.lastModified != -1) {    // Send last modified if available
        val date = "%1$ta, %1$te %1$tb %1$tY %1$tT %1$tZ".format(response.content.lastModified)
        servletResponse.setHeader("Last-Modified", date)
      }
      if (gzip) {
        servletResponse.setHeader("Content-Encoding", "gzip")
      } else {
        servletResponse.setContentLength(response.content.contentLength.toInt)    // Only set content-length if not gzipping
      }
      response.content match {
        case content: StreamableResponseContent => {
          val input = content.input
          val outputStream = servletResponse.getOutputStream
          val output = if (gzip) {
            new GZIPServletOutputStream(outputStream)      // Use GZIP if supported
          } else {
            outputStream
          }
          ArrayBufferPool.use() {
            case buf => IO.stream(input, output, buf, closeOnComplete = true)
          }
        }
      }
    }
  }
}
