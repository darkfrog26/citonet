package com.outr.citonet.http

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpHeaders(headers: Map[String, List[String]] = Map.empty) {
  def +(header: (String, String)) = {
    val current = headers.getOrElse(header._1, Nil)
    val updated = headers + (header._1 -> (header._2 :: current.reverse).reverse)
    copy(headers = updated)
  }

  def get(name: String) = headers.get(name)

  def getFirst(name: String) = get(name).map(l => l.head)
}