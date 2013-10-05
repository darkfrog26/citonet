package com.outr.citonet.http.filter

import com.outr.citonet.http.request.HttpRequest
import com.outr.citonet.http.response.HttpResponse

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpFilter {
  def priority: Double

  def filter(request: HttpRequest): Either[HttpRequest, HttpResponse]
}

object HttpFilter {
  val Lowest = Double.MinValue
  val Lower = 0.0
  val Low = 0.5
  val Normal = 1.0
  val High = 2.0
  val Higher = 10.0
  val Highest = Double.MaxValue
}