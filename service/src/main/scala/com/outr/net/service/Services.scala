package com.outr.net.service

import com.outr.net.http.WebApplication

import scala.util.matching.Regex

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Services {
  def application: WebApplication
  def path: String

  protected def service[In, Out](name: String)(f: In => Out)(implicit manifest: Manifest[In], outManifest: Manifest[Out]) = {
    val s = Service(f)(manifest, outManifest)
    val p = if (path.endsWith("/")) path else s"$path/"
    s.bindTo(application, s"${p}${name}")
    s
  }

  protected def service[In, Out](regex: Regex)(f: In => Out)(implicit inManifest: Manifest[In], outManifest: Manifest[Out]) = {
    val s = Service(f)(manifest, outManifest)
    application.addHandler(s, regex)
    s
  }
}
