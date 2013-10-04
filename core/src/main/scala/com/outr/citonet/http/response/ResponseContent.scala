package com.outr.citonet.http.response

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait ResponseContent {
  def contentType: String
  def contentLength: Long
  def lastModified: Long
}