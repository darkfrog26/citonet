package com.outr.net

import org.json4s.JsonAST.JString
import org.powerscala.json.MapSupport

import scala.language.implicitConversions
import scala.util.matching.Regex

/**
 * @author Matt Hicks <matt@outr.com>
 */
package object service {
  implicit def string2Binder(name: String): ServiceBinder = NamedServiceBinder(name)
  implicit def regex2Binder(regex: Regex): ServiceBinder = RegexServiceBinder(regex)

  def excludeClassFromOutput[C](implicit manifest: Manifest[C]) = MapSupport.o2j.removeWhen("class", JString(manifest.runtimeClass.getName))
}
