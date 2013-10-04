package com.outr.citonet.http

import org.powerscala.property.{ListProperty, Property}
import com.outr.citonet.HasHostAndPort
import org.powerscala.event.Listenable

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HttpApplication extends Listenable {
  val bindings = new Property[List[HasHostAndPort]](default = Some(Nil)) with ListProperty[HasHostAndPort]

  def onReceive(request: HttpRequest): HttpResponse
}
