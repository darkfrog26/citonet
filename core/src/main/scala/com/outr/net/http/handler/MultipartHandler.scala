package com.outr.net.http.handler

import com.outr.net.http.HttpHandler
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import org.powerscala.log.Logging
import com.outr.net.http.content.{StringContent, ContentType, StreamableContent}
import org.apache.commons.fileupload.{FileUpload, RequestContext}
import java.io.{File, InputStream}
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import scala.collection.JavaConversions._

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait MultipartHandler extends HttpHandler with Logging {
  def onReceive(request: HttpRequest, response: HttpResponse) = {
    request.content match {
      case Some(content) => if (content.contentType.isMultipart) {
        val input = content match {
          case c: StreamableContent => c.input
          case _ => throw new RuntimeException(s"Unsupported content: ${content.getClass}")
        }
        val length = content.contentLength
        val upload = new FileUpload(MultipartHandler.FileFactory)
        val items = upload.parseRequest(MultipartRequestContext("UTF-8", length, content.contentType, input))
        items.foreach {
          case item => {
            if (item.isFormField) {
              onField(item.getFieldName, item.getString)
            } else {
              val file = File.createTempFile(item.getName, "outrnet")
              try {
                item.write(file)
                onFile(item.getName, file)
              } finally {
                if (!file.delete()) {
                  warn(s"Unable to delete temporary file: ${file.getAbsolutePath}")
                  file.deleteOnExit()
                }
              }
            }
            item.delete()
          }
        }
        finish(request, response)
      } else {
        failure(request, response, s"${request.url} expected multipart content, but ${content.contentType} content was sent.")
      }
      case None => failure(request, response, s"${request.url} expected multipart content, but no content was sent.")
    }
  }

  def onField(name: String, value: String): Unit

  def onFile(filename: String, file: File): Unit

  protected def failure(request: HttpRequest, response: HttpResponse, reason: String) = {
    response.copy(status = HttpResponseStatus.InternalServerError, content = StringContent(reason))
  }

  def finish(request: HttpRequest, response: HttpResponse): HttpResponse
}

object MultipartHandler {
  val FileFactory = new DiskFileItemFactory()
}

case class MultipartRequestContext(encoding: String, length: Long, contentType: ContentType, input: InputStream) extends RequestContext {
  def getCharacterEncoding = encoding

  def getContentType = contentType.toString

  def getContentLength = length.toInt

  def getInputStream = input
}