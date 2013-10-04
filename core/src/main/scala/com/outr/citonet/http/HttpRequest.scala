package com.outr.citonet.http

import com.outr.citonet.URL

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpRequest(url: URL, headers: Map[String, String])