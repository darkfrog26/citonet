package com.outr.net.http.handler

import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import org.powerscala.Priority
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
object CachedHandler extends HandlerListener with Logging {
  def priority = Priority.Lowest    // Should be lowest so content is already set

  def process(request: HttpRequest, response: HttpResponse) = {
    val cached = request.headers.IfModifiedSince match {
      case Some(modified) if response.content != null && response.content.lastModified != -1 => {
        modified >= response.content.lastModified
      }
      case _ => false
    }
    if (cached) {
      response.copy(status = HttpResponseStatus.NotModified, content = null)
    } else {
      response
    }
  }
}