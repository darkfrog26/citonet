package com.outr.citonet.http

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpResponse(contentType: String, status: HttpResponseStatus, content: ResponseContent)