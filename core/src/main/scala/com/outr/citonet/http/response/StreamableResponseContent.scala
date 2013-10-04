package com.outr.citonet.http.response

import java.io.InputStream

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait StreamableResponseContent extends ResponseContent {
  def input: InputStream
}
