package com.outr.citonet.http.servlet

import com.outr.citonet.http._
import com.outr.citonet.{ArrayBufferPool, URL}
import scala.collection.JavaConversions._
import org.powerscala.IO
import com.outr.citonet.http.response.{StreamableResponseContent, HttpResponse}
import com.outr.citonet.http.request.HttpRequest

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

  def convert(response: HttpResponse, servletResponse: javax.servlet.http.HttpServletResponse) = {
    servletResponse.setStatus(response.status.code)
    if (response.content != null) {
      servletResponse.setContentType(response.content.contentType)
      servletResponse.setContentLength(response.content.contentLength.toInt)
      if (response.content.lastModified != -1) {    // Send last modified if available
        val date = "%1$ta, %1$te %1$tb %1$tY %1$tT %1$tZ".format(response.content.lastModified)
        servletResponse.setHeader("Last-Modified", date)
      }
      response.content match {
        case content: StreamableResponseContent => {
          val input = content.input
          try {
            val output = servletResponse.getOutputStream
            try {
              ArrayBufferPool.use() {
                case buf => IO.stream(input, output, buf, closeOnComplete = false)
              }
            } finally {
              output.flush()
              output.close()
            }
          } finally {
            input.close()
          }
        }
      }
    }
  }
}
