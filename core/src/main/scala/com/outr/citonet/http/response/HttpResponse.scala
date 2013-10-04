package com.outr.citonet.http.response

import com.outr.citonet.http.Cookie

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpResponse(content: ResponseContent = null,
                        status: HttpResponseStatus = HttpResponseStatus.OK,
                        headers: HttpResponseHeaders = HttpResponseHeaders(),
                        cookies: Set[Cookie] = Set.empty) {
  def setCookie(cookie: Cookie) = copy(cookies = cookies + cookie)
}