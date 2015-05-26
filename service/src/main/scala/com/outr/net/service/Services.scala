package com.outr.net.service

import com.outr.net.http.WebApplication
import org.json4s.JsonAST.JString
import org.powerscala.json.MapSupport

import scala.language.implicitConversions
import scala.util.matching.Regex

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Services {
  def application: WebApplication
  def path: String

  implicit def string2Binder(name: String): ServiceBinder = NamedServiceBinder(name)
  implicit def regex2Binder(regex: Regex): ServiceBinder = RegexServiceBinder(regex)

  def excludeClassFromOutput[C](implicit manifest: Manifest[C]) = MapSupport.o2j.removeWhen("class", JString(manifest.runtimeClass.getName))

  protected def service[In, Out](binder: ServiceBinder, excludeClass: Boolean = true)(f: In => Out)(implicit manifest: Manifest[In], outManifest: Manifest[Out]) = {
    if (excludeClass) excludeClassFromOutput[Out]
    val service = Service(f)(manifest, outManifest)
    binder.bindTo(application, path, service)
    service
  }
}

trait ServiceBinder {
  def bindTo[In, Out](application: WebApplication, path: String, service: Service[In, Out]): Unit
}

case class NamedServiceBinder(name: String) extends ServiceBinder {
  override def bindTo[In, Out](application: WebApplication, path: String, service: Service[In, Out]) = {
    val p = if (path.endsWith("/")) path else s"$path/"
    service.bindTo(application, s"${p}${name}")
  }
}

case class RegexServiceBinder(regex: Regex) extends ServiceBinder {
  override def bindTo[In, Out](application: WebApplication, path: String, service: Service[In, Out]) = {
    application.addHandler(service, regex)
  }
}