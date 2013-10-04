package com.outr.citonet.http.request

import com.outr.citonet.URL
import com.outr.citonet.http.HttpRequestHeaders

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpRequest(url: URL, headers: HttpRequestHeaders)