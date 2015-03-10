package com.outr.net.http.servlet

import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

import com.outr.net.{IP, Method, ArrayBufferPool, URL}
import scala.collection.JavaConversions._
import org.powerscala.IO
import com.outr.net.http.request.{HttpRequestHeaders, HttpRequest}
import javax.servlet.http
import com.outr.net.http.content._
import java.nio.charset.Charset
import com.outr.net.http.HttpParameters
import com.outr.net.http.content.InputStreamContent
import org.powerscala._
import com.outr.net.http.response.HttpResponse
import com.outr.net.http.content.StringContent
import org.powerscala.log.Logging

import scala.collection.immutable.ListMap

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ServletConversion extends Logging {
  val IgnoredHeaders = List("Content-Encoding", "Content-Length")

  def convert(servletRequest: javax.servlet.http.HttpServletRequest) = {
    val requestURL = servletRequest.getRequestURL.toString
    val contentType = ContentType.parse(servletRequest.getContentType)
    val method = Method(servletRequest.getMethod)

    val content = servletRequest.getInputStream match {
      case null => None
      case input => Some(InputStreamContent(input, contentType, servletRequest.getContentLength, lastModified = -1L))
    }
    val paramsList = servletRequest.getParameterNames.map {
      case name => name -> servletRequest.getParameterValues(name).toList
    }.toList
    val params = HttpParameters(ListMap(paramsList: _*))
    val url = URL.parse(requestURL).getOrElse(throw new NullPointerException(s"Unable to parse: [$requestURL]")).copy(ip = IP(servletRequest.getLocalAddr), parameters = params)

    val headerMap = servletRequest.getHeaderNames.map{ name => name -> getSanitizedHeader(name, servletRequest)}.toMap

    val headers = HttpRequestHeaders(headerMap)
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

  /**
   * If a header's value is empty, Jetty 9 returns a null value according to this: http://dev.eclipse.org/mhonarc/lists/jetty-users/msg03338.html
   * However, this contradicts the Java EE spec which says that a null return value means that the header is not present.
   * So we sanitise the return value of Jetty 9 to be non-null. This also avoids null propogation throught the code base.
   */
  private def getSanitizedHeader(name:String, servletRequest: javax.servlet.http.HttpServletRequest) = {
    val value = servletRequest.getHeader(name)
    if (value == null) "" else value
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
        if (cookie.domain != null) {
          servletCookie.setDomain(cookie.domain)
        } //else {
          //servletCookie.setDomain(request.url.host)
        //}
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
      try {
        response.content match {
          case content: StreamableContent => {
            val input = content.input
            ArrayBufferPool.use() {
              case buf => IO.stream(input, output, buf, closeOnComplete = true)
            }
            output.close()
          }
          case content: StreamingContent => {
            content.stream(output)
          }
          case content: StringContent => {
            output.write(content.value.getBytes(Charset.forName("UTF-8")))
          }
        }
      } catch {
        case t: Throwable => t.rootCause match {
          case exc: SocketTimeoutException => warn(s"Socket Timeout while writing response from ${request.url} (${t.getMessage}).")
          case exc: TimeoutException => warn(s"Timeout while writing response from ${request.url} (${t.getMessage}).")
          case _ => throw t
        }
      } finally {
        ignoreExceptions(output.flush())
        ignoreExceptions(output.close())
      }
    }
  }
}
