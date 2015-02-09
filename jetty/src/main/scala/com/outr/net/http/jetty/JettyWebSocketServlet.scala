package com.outr.net.http.jetty

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import com.outr.net.communicate._
import com.outr.net.http.HttpApplication
import com.outr.net.http.servlet.ServletConversion
import com.outr.net.http.session.SessionApplication
import org.eclipse.jetty.websocket.api.{Session, WebSocketListener}
import org.eclipse.jetty.websocket.servlet.{WebSocketServletFactory, WebSocketServlet}
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
class JettyWebSocketServlet(handler: JettyHandler) extends WebSocketServlet with Logging {
  override def configure(factory: WebSocketServletFactory) = {
    factory.register(classOf[JettyWebSocket])
  }

  override def service(servletRequest: HttpServletRequest, servletResponse: HttpServletResponse) = {
    val request = try {
      ServletConversion.convert(servletRequest)
    } catch {
      case t: Throwable => {
        error(s"Error occurred while parsing request: ${servletRequest.getRequestURL}", t)
        throw t
      }
    }
    // Make sure the session is available in the WebSocket
    handler.application match {
      case sa: SessionApplication[_] => sa.lookupAndStoreSession(request)
      case _ => // Ignore non Session application
    }
    handler.application.around(request) {
      super.service(servletRequest, servletResponse)
    }
  }
}

class JettyWebSocket extends WebSocketListener with Logging with Connection {
  // TODO: investigate WebSocketCreator to take the place of this (factory.setCreator(...)) - http://stackoverflow.com/questions/15111571/jetty-9-websocketlistener-beforeconnect/15116450#15116450
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
    debug(s"onWebSocketTest: $message")
    HttpApplication.around(request) {
      textEvent.fire(TextMessage(message, this))
    }
  }

  def send(message: String) = if (session != null) {
    session.getRemote.sendString(message)
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