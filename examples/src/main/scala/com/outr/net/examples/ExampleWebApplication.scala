package com.outr.net.examples

import com.outr.net._
import com.outr.net.http.WebApplication
import com.outr.net.communicator.server.CommunicatorHandler
import com.outr.net.http.content.URLContent

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ExampleWebApplication extends WebApplication {
  def init() = {
    addClassPath("/", "html/")
    addContent("/communicator.js", URLContent(getClass.getClassLoader.getResource("communicator.js")))
    addClassPath("/GWTCommunicator/", "GWTCommunicator/")
    addHandler("/Communicator/connect.html", CommunicatorHandler)
  }
}
