package com.outr.citonet.http.response

import com.outr.citonet.http.HttpResponseHeaders

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpResponse(content: ResponseContent = null,
                        status: HttpResponseStatus = HttpResponseStatus.OK,
                        headers: HttpResponseHeaders = HttpResponseHeaders())