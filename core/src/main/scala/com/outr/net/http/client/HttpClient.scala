package com.outr.net.http.client

import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseHeaders, HttpResponseStatus, HttpResponse}
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{HttpPost, HttpGet}
import com.outr.net.{URL, Method}
import org.apache.http.impl.client.{CloseableHttpClient, BasicCookieStore, HttpClients}
import org.apache.http.impl.cookie.BasicClientCookie
import org.apache.http.message.BasicNameValuePair
import org.powerscala.concurrent.Time
import java.util.Date
import com.outr.net.http.content._
import org.apache.http.entity.{ContentType => ApacheContentType, StringEntity, InputStreamEntity, HttpEntityWrapper, FileEntity}
import scala.collection.JavaConversions._
import com.outr.net.http.Cookie
import org.powerscala.IO
import java.io.InputStream

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpClient {
  def send(request: HttpRequest): HttpResponse
}

object Test {
  def main(args: Array[String]): Unit = {
    val request = HttpRequest(URL("http://www.outr.com"))
    val response = HttpClient.send(request)
    println(response)
    println(IO.copy(response.content.asInstanceOf[InputStreamContent].input))
  }
}

object HttpClient extends HttpClient {
  def send(request: HttpRequest) = {
    val cookieStore = new BasicCookieStore
    request.cookies.values.foreach {
      case cookie => if (cookie.maxAge >= 0.0) {
        val c = new BasicClientCookie(cookie.name, cookie.value)
        c.setVersion(0)
        c.setDomain(cookie.domain)
        c.setPath(cookie.path)
        c.setComment(cookie.comment)
        c.setSecure(cookie.secure)
        if (cookie.maxAge > 0.0) {
          c.setExpiryDate(new Date(System.currentTimeMillis() + Time.millis(cookie.maxAge)))
        }
        cookieStore.addCookie(c)
      }
    }
    val client = HttpClients.custom().setDefaultCookieStore(cookieStore).build()
    val clientRequest = request.method match {
      case Method.Get => new HttpGet(request.url.toString)
      case Method.Post => {
        val post = new HttpPost(request.url.toString)
        request.content match {
          case Some(c) => c match {
            case fc: FileContent => post.setEntity(new FileEntity(fc.file))
            case isc: InputStreamContent => post.setEntity(new InputStreamEntity(isc.input, isc.contentLength, ApacheContentType.create(isc.contentType.mimeType)))
            case sc: StringContent => post.setEntity(new StringEntity(sc.value))
            case fpc: FormPostContent => {
              val params = fpc.parameters.values.flatMap {
                case (key, values) => values.map(v => new BasicNameValuePair(key, v))
              }.toList
              val entity = new UrlEncodedFormEntity(params)
              post.setEntity(entity)
            }
            case _ => throw new RuntimeException(s"Unsupported content HttpContent type: ${c.getClass.getName}.")
          }
          case None => // No content to send with POST
        }
        post
      }
    }
    request.headers.values.foreach {
      case (key, value) => if (key != "Content-Length") {   // Content-Length is special and is set above if set
        clientRequest.setHeader(key, value)
      }
    }
    // Process response
    val clientResponse = client.execute(clientRequest)
    val content = clientResponse.getEntity match {
      case null => null
      case r: HttpEntityWrapper => InputStreamContent(HttpClientInputStream(client, r.getContent), ContentType.parse(r.getContentType.getValue), r.getContentLength, System.currentTimeMillis())
    }
    val status = HttpResponseStatus.byCode(clientResponse.getStatusLine.getStatusCode)
    val headers = HttpResponseHeaders(clientResponse.getAllHeaders.map(h => h.getName -> h.getValue).toMap)
    val cookies = cookieStore.getCookies.map {
      case cookie => {
        val name = cookie.getName
        val value = cookie.getValue
        val comment = cookie.getComment
        val commentURL = cookie.getCommentURL
        val domain = cookie.getDomain
        val expiryDate = cookie.getExpiryDate match {
          case null => 0L
          case d => d.getTime
        }
        val ports = cookie.getPorts match {
          case null => Set.empty[Int]
          case p => p.toSet
        }
        Cookie(name, value, comment, commentURL, domain, maxAge = System.currentTimeMillis() - expiryDate, path = cookie.getPath, ports = ports, secure = cookie.isSecure, version = cookie.getVersion)
      }
    }.toSet
    HttpResponse(content, status, headers, cookies)
  }
}

case class HttpClientInputStream(client: CloseableHttpClient, wrapped: InputStream) extends InputStream {
  override def markSupported() = wrapped.markSupported()

  override def reset() = wrapped.reset()

  override def mark(readlimit: Int) = wrapped.mark(readlimit)

  override def close() = {
    wrapped.close()
    client.close()    // Make sure the HttpClient gets closed
  }

  override def available() = wrapped.available()

  override def skip(n: Long) = wrapped.skip(n)

  override def read(b: Array[Byte], off: Int, len: Int) = wrapped.read(b, off, len)

  override def read(b: Array[Byte]) = wrapped.read(b)

  override def read() = wrapped.read()
}