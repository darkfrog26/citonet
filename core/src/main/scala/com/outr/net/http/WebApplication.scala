package com.outr.net.http

import java.io.File

import com.outr.net.http.handler._
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.content.{URLContent, HttpContent, StringContent}
import com.outr.net.http.response.HttpResponse
import com.outr.net.http.session.{Session, SessionApplication}
import org.powerscala.Priority

/**
 * @author Matt Hicks <matt@outr.com>
 */
abstract class WebApplication extends SessionApplication with NotFoundApplication {
  def addContent(creator: => HttpContent, uris: String*): HttpHandler = {
    addContent(request => creator, uris: _*)
  }

  def addContent(creator: HttpRequest => HttpContent, uris: String*): HttpHandler = {
    addHandler(HttpHandler(request => HttpResponse(creator(request))), uris: _*)
  }

  def addHandler(handler: HttpHandler, uris: String*): HttpHandler = {
    uris.foreach {
      case uri => PathMappingHandler.add(this, uri, handler)
    }
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
                  directory: File,
                  allowCaching: Boolean = true,
                  priority: Priority = Priority.Normal) = {
    handlers.add(FileLoadingLookupHandler(urlBasePath, directory, allowCaching = allowCaching), priority)
  }

  def register(path: String, resource: String): Unit = {
    val url = getClass.getClassLoader.getResource(resource)
    if (url == null) throw new NullPointerException(s"Unable to find resource in class loader: $resource.")
    addContent(URLContent(url), path)
  }

  def register(path: String, url: java.net.URL): Unit = {
    addContent(URLContent(url), path)
  }

  def register(resource: String): Unit = {
    val path = if (resource.startsWith("/")) {
      resource
    } else {
      s"/$resource"
    }
    register(path, resource)
  }

  protected def notFoundContent(request: HttpRequest) = {
    StringContent(s"404 Page not found: ${request.url}")
  }
}
