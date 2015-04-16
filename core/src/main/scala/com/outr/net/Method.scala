package com.outr.net

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 * @author Matt Hicks <matt@outr.com>
 */
sealed abstract class Method private(val value: String) extends EnumEntry

object Method extends Enumerated[Method] {
  case object Get extends Method("get")
  case object Put extends Method("put")
  case object Trace extends Method("trace")
  case object Connect extends Method("connect")
  case object Head extends Method("head")
  case object Delete extends Method("delete")
  case object Patch extends Method("patch")
  case object Post extends Method("post")
  case object Options extends Method("options")

  val values = findValues.toVector
}
