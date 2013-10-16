package com.outr.net.http.session

import com.outr.net.http.HttpApplication
import com.outr.net.http.request.HttpRequest

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait SessionApplication[S <: Session] extends HttpApplication {
  protected def loadSession(request: HttpRequest): S

  override def receive(request: HttpRequest) = {
    super.receive(request)
  }
}