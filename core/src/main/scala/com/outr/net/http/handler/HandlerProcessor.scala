package com.outr.net.http.handler

import org.powerscala.event.{EventState, Listenable}
import org.powerscala.event.processor.EventProcessor
import com.outr.net.http.response.HttpResponse
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.HttpHandler
import org.powerscala.Priority

/**
 * HandlerProcessor takes in (HttpRequest, HttpResponse) and returns HttpResponse.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class HandlerProcessor(implicit val listenable: Listenable) extends EventProcessor[(HttpRequest, HttpResponse), HttpResponse, HttpResponse] {
  lazy val name = HandlerProcessor.Name
  val eventManifest = implicitly[Manifest[(HttpRequest, HttpResponse)]]

  protected def handleListenerResponse(value: HttpResponse, state: EventState[(HttpRequest, HttpResponse)]) = {
    state.event = state.event._1 -> value
  }

  protected def responseFor(state: EventState[(HttpRequest, HttpResponse)]) = {
    state.event._2
  }

  def add(handler: HttpHandler, priority: Priority = Priority.Normal) = on({
    case (request, response) => handler.onReceive(request, response)
  }, priority)
}

object HandlerProcessor {
  val Name = "handlerProcessor"
}