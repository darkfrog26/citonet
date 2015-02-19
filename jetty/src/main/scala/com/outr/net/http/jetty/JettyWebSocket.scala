package com.outr.net.http.jetty

import com.outr.net.communicate._
import com.outr.net.http.HttpApplication
import org.eclipse.jetty.websocket.api.{Session, WebSocketListener}
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
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