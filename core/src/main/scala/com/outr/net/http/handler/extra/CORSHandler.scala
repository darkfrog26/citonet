package com.outr.net.http.handler.extra

import com.outr.net.http.handler.AddHeaderHandler

/**
 * CORSHandler provides a convenience for allowing all origins access when added.
 *
 * @author Matt Hicks <matt@outr.com>
 */
object CORSHandler extends AddHeaderHandler("Access-Control-Allow-Origin", "*")