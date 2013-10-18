package com.outr.net.http.content

import java.io.OutputStream

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait StreamingContent extends HttpContent {
  def stream(output: OutputStream): Unit
}
