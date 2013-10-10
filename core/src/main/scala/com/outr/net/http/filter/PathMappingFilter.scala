package com.outr.net.http.filter

import com.outr.net.http.request.HttpRequest
import com.outr.net.http.HttpHandler
import org.powerscala.Storage

/**
 * @author Matt Hicks <matt@outr.com>
 */
private class PathMappingFilter extends HttpFilter {
  private var map = Map.empty[String, HttpHandler]

  def priority = HttpFilter.High

  def filter(request: HttpRequest) = map.get(request.url.path) match {
    case Some(handler) => Right(handler.onReceive(request))
    case None => Left(request)
  }
}

object PathMappingFilter {
  def add(application: FilteredApplication, path: String, handler: HttpHandler) = application.synchronized {
    val filter = getFilter(application)
    filter.map += path -> handler
  }

  def remove(application: FilteredApplication, path: String) = application.synchronized {
    val filter = getFilter(application)
    filter.map -= path
  }

  private def getFilter(application: FilteredApplication) = {
    Storage.getOrSet(application, "pathMappingFilter", application.addFilter(new PathMappingFilter))
  }
}