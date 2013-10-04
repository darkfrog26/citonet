package com.outr.citonet.http

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
}

object HttpParameters {
  val Empty = HttpParameters()
}