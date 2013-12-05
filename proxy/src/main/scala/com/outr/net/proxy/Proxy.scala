package com.outr.net.proxy

import com.outr.net.http.request.HttpRequest

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Proxy {
  def get(request: HttpRequest): Option[HttpRequest]
}