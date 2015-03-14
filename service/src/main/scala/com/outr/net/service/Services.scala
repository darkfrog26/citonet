package com.outr.net.service

import com.outr.net.http.WebApplication
import com.outr.net.http.session.Session

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
}
