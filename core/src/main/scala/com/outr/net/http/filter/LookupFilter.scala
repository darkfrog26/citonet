package com.outr.net.http.filter

import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse
import com.outr.net.URL
import com.outr.net.http.content.HttpContent

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
