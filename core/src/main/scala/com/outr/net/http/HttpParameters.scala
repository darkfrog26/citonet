package com.outr.net.http

import java.net.URLDecoder

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpParameters(values: Map[String, List[String]] = Map.empty) {
  def +(parameter: (String, String)) = {
    val current = values.getOrElse(parameter._1, Nil)
    val updated = values + (parameter._1 -> (parameter._2 :: current.reverse).reverse)
    copy(values = updated)
  }

  def get(name: String) = values.get(name)

  def getFirst(name: String) = get(name).map(l => l.head)

  def apply(name: String) = get(name).getOrElse(throw new NullPointerException(s"No parameter found for: $name in $values"))

  def first(name: String) = apply(name).head

  def contains(name: String) = values.contains(name)
}

object HttpParameters {
  val Empty = HttpParameters()

  def parse(params: String): HttpParameters = {
    if (params == null) {
      HttpParameters()
    } else if (params.startsWith("?")) {
      parse(params.substring(1))
    } else {
      var parameters = Map.empty[String, List[String]]
      if (params.length > 1) {
        params.split('&').foreach {
          case entry => {
            val split = entry.indexOf('=')
            val (key, value) = if (split == -1) {
              URLDecoder.decode(entry, "utf-8") -> null
            } else {
              URLDecoder.decode(entry.substring(0, split), "utf-8") -> URLDecoder.decode(entry.substring(split + 1), "utf-8")
            }
            val entries = parameters.getOrElse(key, Nil)
            if (value == null) {
              parameters += key -> entries
            } else {
              parameters += key -> (value :: entries.reverse).reverse
            }
          }
        }
      }
      HttpParameters(parameters)
    }
  }
}