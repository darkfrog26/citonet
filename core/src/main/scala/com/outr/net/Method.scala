package com.outr.net

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class Method private(val value: String) extends EnumEntry

object Method extends Enumerated[Method] {
  val Get = new Method("get")
  val Put = new Method("put")
  val Trace = new Method("trace")
  val Connect = new Method("connect")
  val Head = new Method("head")
  val Delete = new Method("delete")
  val Patch = new Method("patch")
  val Post = new Method("post")
  val Options = new Method("options")
}
