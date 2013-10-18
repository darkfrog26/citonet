package com.outr.net.http.response

import com.outr.net.http.Cookie
import com.outr.net.http.content.HttpContent

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpResponse(content: HttpContent = null,
                        status: HttpResponseStatus = HttpResponseStatus.OK,
                        headers: HttpResponseHeaders = HttpResponseHeaders(),
                        cookies: Set[Cookie] = Set.empty) {
  def setCookie(cookie: Cookie) = copy(cookies = cookies + cookie)

  def setHeader(key: String, value: String) = copy(headers = headers.copy(headers.values + (key -> value)))

  /**
   * Merges the contents of this response with the supplied response to create a new HttpResponse object. The supplied
   * response takes priority if there is any overlap.
   *
   * @param response the response to merge
   */
  def merge(response: HttpResponse) = {
    if (content != null && response.content != null) {
      throw new RuntimeException("Cannot merge with two HttpContents!")
    }
    val status = response.status
    val headers = this.headers.merge(response.headers)
    val cookies = this.cookies ++ response.cookies
    HttpResponse(response.content, status, headers, cookies)
  }
}