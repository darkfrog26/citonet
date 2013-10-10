package com.outr.net.http.content

import java.io.InputStream

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait StreamableContent extends HttpContent {
  def input: InputStream
}
