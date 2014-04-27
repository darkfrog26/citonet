package com.outr.net.http

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpHeaders {
  def apply(key: String) = values(key)
  def get(key: String) = values.get(key)

  def values: Map[String, String]
}