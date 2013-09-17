package com.outr.citonet.http

import com.outr.citonet.{Protocol, URL, Method}

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpRequest(method: Method,
                       url: URL,
                       protocol: Protocol,
                       headers: HttpHeaders)