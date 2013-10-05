package com.outr.citonet.http.servlet

import com.outr.citonet.http._
import java.io.File
import com.outr.citonet.http.content.FileContent

/**
 * @author Matt Hicks <matt@outr.com>
 */
object TestWebApplication extends WebApplication {
  def init() = {
    addContent("/test.html", FileContent(new File("test.html")))
    addClassPath("/Communicator/", "Communicator/")
  }
}