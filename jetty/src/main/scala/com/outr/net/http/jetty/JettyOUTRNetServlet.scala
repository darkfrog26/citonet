package com.outr.net.http.jetty

import javax.servlet.ServletConfig
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import com.outr.net.communicate._
import com.outr.net.http.HttpApplication
import com.outr.net.http.servlet.{ServletConversion, OUTRNetServletSupport}
import com.outr.net.http.session.SessionApplication
import org.eclipse.jetty.websocket.api.{Session, WebSocketListener}
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
        case sa: SessionApplication[_] => sa.lookupAndStoreSession(request)
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

class JettyWebSocket(val application: HttpApplication) extends WebSocketListener with Logging with Connection {
  val request = HttpApplication.requestOption.getOrElse(throw new RuntimeException(s"Request not found!"))

  private var session: Session = _

  override def onWebSocketConnect(session: Session) = {
    debug(s"onWebSocketConnect! $session (${session.getProtocolVersion})")
    this.session = session
    HttpApplication.around(request) {
      connected.fire(this)
    }
  }

  override def onWebSocketText(message: String) = {
    debug(s"onWebSocketText: $message, holder: ${holder()}")
    HttpApplication.around(request) {
      textEvent.fire(TextMessage(message, this))
    }
  }

  def send(message: String) = if (session != null) {
    session.getRemote.sendStringByFuture(message)
  }

  override def onWebSocketBinary(payload: Array[Byte], offset: Int, len: Int) = {
    debug(s"onWebSocketBinary!")
    HttpApplication.around(request) {
      binaryEvent.fire(BinaryMessage(payload, offset, len, this))
    }
  }

  override def onWebSocketError(cause: Throwable) = {
    debug(s"onWebSocketError", cause)
    HttpApplication.around(request) {
      errorEvent.fire(ErrorMessage(cause, this))
    }
  }

  override def onWebSocketClose(statusCode: Int, reason: String) = {
    debug(s"onWebSocketClose: $statusCode: $reason")
    this.session = null
    HttpApplication.around(request) {
      disconnectedEvent.fire(DisconnectedMessage(statusCode, reason, this))
    }
  }
}