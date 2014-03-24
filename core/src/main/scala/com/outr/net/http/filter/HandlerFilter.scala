package com.outr.net.http.filter

import com.outr.net.http.HttpHandler
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse

/**
 * HandleFilter implements HttpHandler and support filtering and modifying of requests before ultimately being handled.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait HandlerFilter extends HttpHandler {
  /**
   * True if this request should be handled by this filter.
   *
   * @param request the incoming request
   * @param response the incoming response
   */
  def accept(request: HttpRequest, response: HttpResponse): Boolean

  /**
   * Allows modification of the request before calling handle.
   *
   * @param request the request to modify
   * @return the modified request
   */
  def modify(request: HttpRequest): HttpRequest

  /**
   * Handles the request. Only called if accept returns true.
   *
   * @param request the modified request by modify.
   * @param response the inbound response.
   * @return the outbound response.
   */
  def handle(request: HttpRequest, response: HttpResponse): HttpResponse

  override def onReceive(request: HttpRequest, response: HttpResponse) = if (accept(request, response)) {
    val modified = modify(request)
    handle(modified, response)
  } else {
    response
  }
}
