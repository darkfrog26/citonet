package com.outr.net.http.jetty

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import com.outr.net.communicate._
import org.eclipse.jetty.websocket.api.{Session, WebSocketListener}
import org.eclipse.jetty.websocket.servlet.{WebSocketServletFactory, WebSocketServlet}
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
class JettyWebSocketServlet extends WebSocketServlet {
  override def configure(factory: WebSocketServletFactory) = {
    factory.register(classOf[JettyWebSocket])
  }

  override def service(request: HttpServletRequest, response: HttpServletResponse) = {
    super.service(request, response)
  }
}

class JettyWebSocket extends WebSocketListener with Logging with Connection {
  private var session: Session = _

  override def onWebSocketConnect(session: Session) = {
    debug(s"onWebSocketConnect! $session (${session.getProtocolVersion})")
    this.session = session
    connected.fire(this)
  }

  override def onWebSocketText(message: String) = {
    debug(s"onWebSocketTest: $message")
    text.fire(TextMessage(message, this))
  }

  def send(message: String) = if (session != null) {
    session.getRemote.sendString(message)
  }

  override def onWebSocketBinary(payload: Array[Byte], offset: Int, len: Int) = {
    debug(s"onWebSocketBinary!")
    binary.fire(BinaryMessage(payload, offset, len, this))
  }

  override def onWebSocketError(cause: Throwable) = {
    debug(s"onWebSocketError", cause)
    error.fire(ErrorMessage(cause, this))
  }

  override def onWebSocketClose(statusCode: Int, reason: String) = {
    debug(s"onWebSocketClose: $statusCode: $reason")
    this.session = null
    disconnected.fire(DisconnectedMessage(statusCode, reason, this))
  }
}