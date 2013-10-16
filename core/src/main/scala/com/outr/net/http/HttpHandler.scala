package com.outr.net.http

import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpHandler {
  def priority: Double

  def onReceive(request: HttpRequest, response: HttpResponse): HttpResponse
}

object HttpHandler {
  val Lowest = Double.MinValue
  val Lower = 0.0
  val Low = 0.5
  val Normal = 1.0
  val High = 2.0
  val Higher = 10.0
  val Highest = Double.MaxValue

  def apply(f: HttpRequest => HttpResponse, handlerPriority: Double = Normal) = new HttpHandler {
    def priority = handlerPriority

    def onReceive(request: HttpRequest, response: HttpResponse) = response.merge(f(request))
  }
}