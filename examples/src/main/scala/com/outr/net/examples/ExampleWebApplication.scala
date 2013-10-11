package com.outr.net.examples

import com.outr.net.http.WebApplication
import com.outr.net.communicator.server.CommunicatorHandler

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ExampleWebApplication extends WebApplication {
  def init() = {
    addClassPath("/", "html/")
    addClassPath("/Communicator/", "Communicator/")
    addHandler("/Communicator/connect.html", CommunicatorHandler)
  }
}
