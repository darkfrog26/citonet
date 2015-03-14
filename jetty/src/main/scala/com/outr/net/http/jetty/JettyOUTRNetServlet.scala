package com.outr.net.http.jetty

import javax.servlet.ServletConfig
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import com.outr.net.http.HttpApplication
import com.outr.net.http.servlet.{ServletConversion, OUTRNetServletSupport}
import com.outr.net.http.session.SessionApplication
import org.eclipse.jetty.websocket.servlet._
import org.powerscala.log.Logging

/**
 * JettyOUTRNetServlet expands OUTRNetServlet functionality to add additional features specific to Jetty including
 * WebSocket support.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class JettyOUTRNetServlet extends WebSocketServlet with WebSocketCreator with Logging {
  val support = new OUTRNetServletSupport

  override def configure(factory: WebSocketServletFactory) = {
    factory.setCreator(this)
  }

  def init(application: HttpApplication) = {
    support.init(application)
  }

  override def init(config: ServletConfig) = {
    support.init(config)

    super.init(config)
  }

  override def destroy() = {
    support.destroy()

    super.destroy()
  }

  override def service(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) = {
    if (servletRequest.getRequestURI == support.application.webSocketPath.orNull) {    // WebSocket
    val request = try {
        ServletConversion.convert(servletRequest)
      } catch {
        case t: Throwable => {
          error(s"Error occurred while parsing request: ${servletRequest.getRequestURL}", t)
          throw t
        }
      }
      // Make sure the session is available in the WebSocket
      support.application match {
        case sa: SessionApplication => sa.lookupAndStoreSession(request)
        case _ => // Ignore non Session application
      }
      support.application.around(request) {
        super.service(servletRequest, servletResponse)
      }
    } else {                                                                          // All other requests
      support.handle(servletRequest, servletResponse)
    }
  }

  override def createWebSocket(request: ServletUpgradeRequest, response: ServletUpgradeResponse) = {
    new JettyWebSocket(support.application)
  }
}