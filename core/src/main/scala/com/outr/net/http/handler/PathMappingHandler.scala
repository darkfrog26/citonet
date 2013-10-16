package com.outr.net.http.handler

import com.outr.net.http.request.HttpRequest
import com.outr.net.http.HttpHandler
import org.powerscala.Storage
import com.outr.net.http.response.HttpResponse

/**
 * @author Matt Hicks <matt@outr.com>
 */
private class PathMappingHandler extends HttpHandler {
  private var map = Map.empty[String, HttpHandler]

  def priority = HttpHandler.High

  def onReceive(request: HttpRequest, response: HttpResponse) = map.get(request.url.path) match {
    case Some(handler) => handler.onReceive(request, response)
    case None => response
  }
}

object PathMappingHandler {
  def add(application: HandlerApplication, path: String, handler: HttpHandler) = application.synchronized {
    val pmh = getHandler(application)
    pmh.map += path -> handler
  }

  def remove(application: HandlerApplication, path: String) = application.synchronized {
    val pmh = getHandler(application)
    pmh.map -= path
  }

  private def getHandler(application: HandlerApplication) = {
    Storage.getOrSet(application, "pathMappingHandler", application.addHandler(new PathMappingHandler))
  }
}