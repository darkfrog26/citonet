package com.outr.net.http.servlet

import com.outr.net.{IP, Method, ArrayBufferPool, URL}
import scala.collection.JavaConversions._
import org.powerscala.IO
import com.outr.net.http.request.{HttpRequestHeaders, HttpRequest}
import javax.servlet.http
import com.outr.net.http.content._
import java.nio.charset.Charset
import com.outr.net.http.HttpParameters
import com.outr.net.http.content.InputStreamContent
import scala.Some
import com.outr.net.http.response.HttpResponse
import com.outr.net.http.content.StringContent
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ServletConversion extends Logging {
  val IgnoredHeaders = List("Content-Encoding", "Content-Length")

  def convert(servletRequest: javax.servlet.http.HttpServletRequest) = {
    val requestURL = servletRequest.getRequestURL.toString
    val contentType = ContentType.parse(servletRequest.getContentType)
    val content = if (servletRequest.getContentLength != -1) {    // TODO: is it possible this might be -1 and there still be content?
      Some(InputStreamContent(servletRequest.getInputStream, contentType, servletRequest.getContentLength, lastModified = -1L))
    } else {
      None
    }
    val params = HttpParameters(servletRequest.getParameterMap.collect {
      case (name, values) => name -> values.toList
    }.toMap)
    val url = URL.parse(requestURL).getOrElse(throw new NullPointerException(s"Unable to parse: [$requestURL]")).copy(ip = IP(servletRequest.getLocalAddr), parameters = params)
    val method = Method(servletRequest.getMethod)
    val headers = HttpRequestHeaders(servletRequest.getHeaderNames.map(name => name -> servletRequest.getHeader(name)).toMap)
    val cookies = headers.parseCookies()
    val remoteAddress = IP(servletRequest.getRemoteAddr)
    val remoteHost = servletRequest.getRemoteHost
    val remotePort = servletRequest.getRemotePort
    val request = HttpRequest(
      url = url,
      method = method,
      headers = headers,
      cookies = cookies,
      content = content,
      remoteAddress = remoteAddress,
      remoteHost = remoteHost,
      remotePort = remotePort
    )
    // Special handling for FORM post data
    if (method == Method.Post && contentType == ContentType.FormURLEncoded && content.nonEmpty) {
      val contentString = request.contentString.get
      request.copy(content = Some(FormPostContent(contentString)))
    } else {
      request
    }
  }

  def convert(request: HttpRequest,
              response: HttpResponse,
              servletResponse: javax.servlet.http.HttpServletResponse,
              gzip: Boolean) = {
    servletResponse.setStatus(response.status.code)
    response.headers.values.foreach {
      case (key, value) => if (!IgnoredHeaders.contains(key)) {
        servletResponse.setHeader(key, value)
      }
    }
    response.cookies.foreach {
      case cookie => {
        val servletCookie = new http.Cookie(cookie.name, cookie.value)
        if (cookie.comment != null) servletCookie.setComment(cookie.comment)
        if (cookie.domain != null) servletCookie.setDomain(cookie.domain)
        servletCookie.setHttpOnly(cookie.httpOnly)
        if (cookie.maxAge != Int.MinValue) servletCookie.setMaxAge(math.round(cookie.maxAge).toInt)
        if (cookie.path != null) servletCookie.setPath(cookie.path)
        servletCookie.setSecure(cookie.secure)
        servletCookie.setVersion(cookie.version)
        servletResponse.addCookie(servletCookie)
      }
    }
    if (response.content != null) {
      // Send the content type
      response.content.contentType match {
        case null => // No content-type to set
        case contentType => servletResponse.setContentType(contentType.toString)
      }
      servletResponse.setCharacterEncoding("UTF-8")
      if (response.content.lastModified != -1) {    // Send last modified if available
        val date = "%1$ta, %1$te %1$tb %1$tY %1$tT %1$tZ".format(response.content.lastModified)
        servletResponse.setHeader("Last-Modified", date)
      }
      if (gzip) {
        servletResponse.setHeader("Content-Encoding", "gzip")
      } else if (response.content.contentLength != -1L) {
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
          outputStream.close()
        }
        case content: StreamingContent => {
          try {
            content.stream(output)
          } finally {
            try {
              output.flush()
              output.close()
            } catch {
              case t: Throwable => // Ignore issues trying to flush
            }
          }
        }
        case content: StringContent => {
          try {
            output.write(content.value.getBytes(Charset.forName("UTF-8")))
          } finally {
            try {
              output.flush()
              output.close()
            } catch {
              case t: Throwable => // Ignore issues trying to flush
            }
          }
        }
      }
    }
  }
}
