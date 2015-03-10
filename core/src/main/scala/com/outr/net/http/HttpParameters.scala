package com.outr.net.http

import java.net.{URLEncoder, URLDecoder}

import scala.collection.immutable.ListMap

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpParameters(values: ListMap[String, List[String]] = ListMap.empty, isEncoded: Boolean = true) {
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

  private def encode(s: String) = URLEncoder.encode(s, "UTF-8")
  private def decode(s: String) = URLDecoder.decode(s, "UTF-8")

  def encoded = if (isEncoded) {
    this
  } else {
    val enc = values.map {
      case (k, vs) => encode(k) -> vs.map(encode)
    }
    copy(values = enc, isEncoded = true)
  }

  def decoded = if (isEncoded) {
    val dec = values.map {
      case (k, vs) => decode(k) -> vs.map(decode)
    }
    copy(values = dec, isEncoded = false)
  } else {
    this
  }
}

object HttpParameters {
  val Empty = HttpParameters()

  def parse(params: String, encoded: Boolean): HttpParameters = {
    if (params == null) {
      HttpParameters()
    } else if (params.startsWith("?")) {
      parse(params.substring(1), encoded)
    } else {
      var parameters = ListMap.empty[String, List[String]]
      if (params.length > 1) {
        params.split('&').foreach {
          case entry => {
            val split = entry.indexOf('=')
            val (key, value) = if (split == -1) {
              entry -> null
            } else {
              entry.substring(0, split) -> entry.substring(split + 1)
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
      HttpParameters(parameters, isEncoded = encoded)
    }
  }
}