package com.outr.citonet.http.filter

import com.outr.citonet.http.request.HttpRequest
import com.outr.citonet.http.response.HttpResponse
import com.outr.citonet.URL
import com.outr.citonet.http.content.HttpContent

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait LookupFilter extends HttpFilter {
  def lookup(url: URL): Option[HttpContent]

  def filter(request: HttpRequest) = lookup(request.url) match {
    case Some(content) => Right(HttpResponse(content = content))
    case None => Left(request)
  }
}
