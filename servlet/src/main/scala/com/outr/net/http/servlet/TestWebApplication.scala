package com.outr.net.http.servlet

import com.outr.net.http._
import java.io.File
import com.outr.net.http.content.FileContent

/**
 * @author Matt Hicks <matt@outr.com>
 */
object TestWebApplication extends WebApplication {
  def init() = {
    addContent("/test.html", FileContent(new File("test.html")))
    addClassPath("/Communicator/", "Communicator/")
  }
}