package com.outr.net.http.servlet

import com.outr.net.{Method, ArrayBufferPool, URL}
import scala.collection.JavaConversions._
import org.powerscala.IO
import com.outr.net.http.response.HttpResponse
import com.outr.net.http.request.{HttpRequestHeaders, HttpRequest}
import javax.servlet.http
import com.outr.net.http.content.{InputStreamContent, StringContent, StreamableContent}
import java.nio.charset.Charset

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ServletConversion {
  def convert(servletRequest: javax.servlet.http.HttpServletRequest) = {
    val requestURL = servletRequest.getRequestURL.toString
    val url = URL.parse(requestURL).getOrElse(throw new NullPointerException(s"Unable to parse: [$requestURL]"))
    val method = Method(servletRequest.getMethod)
    val headers = servletRequest.getHeaderNames.map(name => name -> servletRequest.getHeader(name)).toMap
    val content = if (servletRequest.getContentLength != -1) {    // TODO: is it possible this might be -1 and there still be content?
      Some(InputStreamContent(servletRequest.getInputStream, servletRequest.getContentType, servletRequest.getContentLength, lastModified = -1L))
    } else {
      None
    }
    val request = HttpRequest(url, method, HttpRequestHeaders(headers), content = content)
    request
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
      // Send the content type
      servletResponse.setContentType(response.content.contentType)
      servletResponse.setCharacterEncoding("UTF-8")
      if (response.content.lastModified != -1) {    // Send last modified if available
        val date = "%1$ta, %1$te %1$tb %1$tY %1$tT %1$tZ".format(response.content.lastModified)
        servletResponse.setHeader("Last-Modified", date)
      }
      if (gzip) {
        servletResponse.setHeader("Content-Encoding", "gzip")
      } else {
        servletResponse.setContentLength(response.content.contentLength.toInt)    // Only set content-length if not gzipping
      }

      val outputStream = servletResponse.getOutputStream
      val output = if (gzip) {
        new GZIPServletOutputStream(outputStream)      // Use GZIP if supported
      } else {
        outputStream
      }
      response.content match {
        case content: StreamableContent => {
          val input = content.input
          ArrayBufferPool.use() {
            case buf => IO.stream(input, output, buf, closeOnComplete = true)
          }
        }
        case content: StringContent => {
          try {
            output.write(content.value.getBytes(Charset.forName("UTF-8")))
          } finally {
            output.flush()
            output.close()
          }
        }
      }
    }
  }
}
