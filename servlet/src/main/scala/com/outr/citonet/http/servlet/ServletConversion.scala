package com.outr.citonet.http.servlet

import com.outr.citonet.http.{URLResponseContent, HttpResponse, HttpRequest}
import com.outr.citonet.{ArrayBufferPool, URL}
import scala.collection.JavaConversions._
import org.powerscala.IO

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ServletConversion {
  def convert(servletRequest: javax.servlet.http.HttpServletRequest) = {
    val requestURL = servletRequest.getRequestURL.toString
    val url = URL.parse(requestURL).getOrElse(throw new NullPointerException(s"Unable to parse: [$requestURL]"))
    val headers = servletRequest.getHeaderNames.map(name => name -> servletRequest.getHeader(name)).toMap
    HttpRequest(url, headers)
  }

  def convert(response: HttpResponse, servletResponse: javax.servlet.http.HttpServletResponse) = {
    servletResponse.setStatus(response.status.code)
    servletResponse.setContentType(response.contentType)
    response.content match {
      case content: URLResponseContent => {
        val connection = content.url.javaURL.openConnection()
        servletResponse.setContentLength(connection.getContentLength)

        val input = connection.getInputStream
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
