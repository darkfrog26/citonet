package com.outr.net.http.handler

import com.outr.net.http.request.HttpRequest
import com.outr.net.http.HttpHandler
import org.powerscala.Storage
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}

/**
 * @author Matt Hicks <matt@outr.com>
 */
private class PathMappingHandler extends HttpHandler {
  private var map = Map.empty[String, HttpHandler]

  def onReceive(request: HttpRequest, response: HttpResponse) = if (response.status == HttpResponseStatus.NotFound) {
    map.get(request.url.path) match {
      case Some(handler) => handler.onReceive(request, response)
      case None => response
    }
  } else {
    response
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

  private def getHandler(application: HandlerApplication) = synchronized {
    Storage.get[String, PathMappingHandler](application, "pathMappingHandler") match {
      case Some(handler) => handler
      case None => {
        val handler = new PathMappingHandler
        Storage.set(application, "pathMappingHandler", handler)
        application.handlers.add(handler)
        handler
      }
    }
  }
}