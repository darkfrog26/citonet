package com.outr.net.http

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpHeaders {
  def apply(key: String) = get(key).getOrElse(throw new NullPointerException(s"Unable to find header for $key."))
  def get(key: String) = values.find(t => t._1.equalsIgnoreCase(key)).map(t => t._2)

  def values: Map[String, String]
}