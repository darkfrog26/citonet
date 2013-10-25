package com.outr.net.http.jetty

import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.Request
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.outr.net.http.HttpApplication
import org.powerscala.log.Logging
import com.outr.net.http.servlet.OUTRNetServlet

/**
 * @author Matt Hicks <matt@outr.com>
 */
class JettyHandler private[jetty](val application: HttpApplication) extends AbstractHandler with Logging {
  def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) = {
    OUTRNetServlet.handle(application, request, response)
    baseRequest.setHandled(true)
  }
}