package com.outr.net.http.handler

import org.powerscala.event.Listener
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse
import org.powerscala.event.processor.EventProcessor

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HandlerListener extends Listener[(HttpRequest, HttpResponse), HttpResponse] {
  def name = HandlerProcessor.Name
  def eventClass = classOf[(HttpRequest, HttpResponse)]
  def modes = EventProcessor.DefaultModes

  final def receive(event: (HttpRequest, HttpResponse)) = event match {
    case (request, response) => process(request, response)
  }

  def process(request: HttpRequest, response: HttpResponse): HttpResponse
}
