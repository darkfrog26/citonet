package com.outr.net.http.jetty

import java.util
import javax.servlet.{ServletContext, ServletConfig}

import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.Request
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.outr.net.http.HttpApplication
import org.eclipse.jetty.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.util.component.LifeCycle
import org.powerscala.log.Logging
import com.outr.net.http.servlet.OUTRNetServlet

/**
 * @author Matt Hicks <matt@outr.com>
 */
class JettyHandler private[jetty](val application: HttpApplication) extends AbstractHandler with Logging {
  private val webSocketHandler = new WebSocketContextHandler(this)

  override def start(l: LifeCycle) = {
    super.start(l)

    webSocketHandler.start(l)
  }

  def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) = {
    if (request.getRequestURI == application.webSocketPath.orNull) {
      webSocketHandler.servlet.service(request, response)
      baseRequest.setHandled(true)
    } else {
      OUTRNetServlet.handle(application, request, response)
      baseRequest.setHandled(true)
    }
  }
}

class WebSocketContextHandler(handle: JettyHandler) extends ServletContextHandler(ServletContextHandler.SESSIONS) {
  val servlet = new JettyWebSocketServlet

  setContextPath("/")
  addServlet(new ServletHolder(servlet), "/websocket")

  override def start(l: LifeCycle): Unit = {
    super.start(l)
    val servletContext = getServletContext
    servlet.init(new ServletConfig {
      override def getInitParameterNames: util.Enumeration[String] = new util.Hashtable[String, String]().elements()

      override def getServletName: String = "JettyWebSocketServlet"

      override def getInitParameter(name: String): String = null

      override def getServletContext: ServletContext = servletContext
    })
  }
}