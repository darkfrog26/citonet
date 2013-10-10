package com.outr.net.http.content

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpContent {
  def contentType: String
  def contentLength: Long
  def lastModified: Long
}
