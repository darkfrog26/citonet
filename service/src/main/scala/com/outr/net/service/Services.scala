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

  implicit def thisWebApplication: WebApplication = application
  implicit def thisServices: Services = this
}

trait ServiceBinder {
  def bindTo[In, Out](application: WebApplication, path: String, service: Service[In, Out]): Unit
  def unbindFrom[In, Out](application: WebApplication, path: String, service: Service[In, Out]): Unit
}

case class NamedServiceBinder(name: String) extends ServiceBinder {
  override def bindTo[In, Out](application: WebApplication, path: String, service: Service[In, Out]) = {
    val p = if (path.endsWith("/")) path else s"$path/"
    val n = if (name.startsWith("/")) name.substring(1) else name
    service.bindTo(application, s"$p$n")
  }

  override def unbindFrom[In, Out](application: WebApplication, path: String, service: Service[In, Out]) = {
    val p = if (path.endsWith("/")) path else s"$path/"
    val n = if (name.startsWith("/")) name.substring(1) else name
    service.unbindFrom(application, s"$p$n")
  }
}

case class RegexServiceBinder(regex: Regex) extends ServiceBinder {
  override def bindTo[In, Out](application: WebApplication, path: String, service: Service[In, Out]) = {
    application.addHandler(service, regex)
  }

  override def unbindFrom[In, Out](application: WebApplication, path: String, service: Service[In, Out]) = {
    throw new RuntimeException("Unbinding not supported for regex services yet.")
  }
}