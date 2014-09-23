package com.outr.net.http.client

import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpClient {
  def send(request: HttpRequest): HttpResponse
}