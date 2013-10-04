package com.outr.citonet.http.filter

import com.outr.citonet.http.request.HttpRequest
import com.outr.citonet.http.response.{ResponseContent, HttpResponse}
import com.outr.citonet.URL

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait LookupFilter extends HttpFilter {
  def lookup(url: URL): Option[ResponseContent]

  def filter(request: HttpRequest) = lookup(request.url) match {
    case Some(content) => Right(HttpResponse(content = content))
    case None => Left(request)
  }
}
