package com.outr.citonet.http.filter

import com.outr.citonet.http.request.HttpRequest
import com.outr.citonet.http.response.HttpResponse

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpFilter {
  def filter(request: HttpRequest): Either[HttpRequest, HttpResponse]
}
