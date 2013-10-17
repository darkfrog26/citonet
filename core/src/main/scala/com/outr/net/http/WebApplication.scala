package com.outr.net.http

import com.outr.net.http.handler._
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.content.HttpContent
import com.outr.net.http.response.HttpResponse
import com.outr.net.http.content.StringContent
import com.outr.net.http.session.{Session, SessionApplication}
import org.powerscala.Priority

/**
 * @author Matt Hicks <matt@outr.com>
 */
abstract class WebApplication[S <: Session] extends SessionApplication[S] with NotFoundApplication {
  def addContent(uri: String, creator: => HttpContent): HttpHandler = {
    addContent(uri, request => creator)
  }

  def addContent(uri: String, creator: HttpRequest => HttpContent): HttpHandler = {
    addHandler(uri, HttpHandler(request => HttpResponse(creator(request))))
  }

  def addHandler(uri: String, handler: HttpHandler): HttpHandler = {
    PathMappingHandler.add(this, uri, handler)
    handler
  }

  def removeContent(uri: String) = {
    PathMappingHandler.remove(this, uri)
  }

  def addClassPath(urlBasePath: String,
                   lookupPath: String,
                   allowCaching: Boolean = true,
                   priority: Priority = Priority.Normal) = {
    handlers.add(ClassLoadingLookupHandler(urlBasePath, lookupPath, allowCaching = allowCaching), priority)
  }

  def addFilePath(urlBasePath: String,
                   lookupPath: String,
                   allowCaching: Boolean = true,
                   priority: Priority = Priority.Normal) = {
    handlers.add(FileLoadingLookupHandler(urlBasePath, lookupPath, allowCaching = allowCaching), priority)
  }

  protected def notFoundContent(request: HttpRequest) = {
    StringContent(s"404 Page not found: ${request.url}")
  }
}
