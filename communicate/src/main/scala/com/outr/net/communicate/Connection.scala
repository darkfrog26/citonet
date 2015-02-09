package com.outr.net.communicate

import org.powerscala.event.Listenable
import org.powerscala.event.processor.UnitProcessor
import org.powerscala.log.Logging
import org.powerscala.property.Property
import org.powerscala.json._

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Connection extends Listenable with Logging {
  val holder = Property[ConnectionHolder]()

  val connected = new UnitProcessor[Connection]("connected")
  val textEvent = new UnitProcessor[TextMessage]("text")
  val binaryEvent = new UnitProcessor[BinaryMessage]("binary")
  val jsonEvent = new UnitProcessor[Any]("json")
  val errorEvent = new UnitProcessor[ErrorMessage]("error")
  val disconnectedEvent = new UnitProcessor[DisconnectedMessage]("disconnected")

  def send(message: String): Unit

  def sendJSON(message: Any) = {
    val s = toJSON(message).compact
    send(s)
  }

  holder.change.on {
    case evt => {
      if (evt.oldValue != null) {
        evt.oldValue.removedConnection.fire(ConnectionRemoved(this))
      }
      if (evt.newValue != null) {
        evt.newValue.addedConnection.fire(ConnectionAdded(evt.oldValue, this))
      }
    }
  }

  connected.on(holder().connected.fire(_))
  textEvent.on(holder().textEvent.fire(_))
  binaryEvent.on(holder().binaryEvent.fire(_))
  jsonEvent.on(holder().jsonEvent.fire(_))
  errorEvent.on(holder().errorEvent.fire(_))
  disconnectedEvent.on(holder().disconnectedEvent.fire(_))

  textEvent.on {
    case evt => if (evt.message == "Ping") {              // Default Ping / Pong support
      send("Pong")
    } else if (evt.message.startsWith("::json::")) {      // JSON support
      try {
        ConnectionHolder.stack.context(this) {
          val json = evt.message.substring(8)
          val obj = fromJSON(json)
          this.jsonEvent.fire(obj)
        }
      } catch {
        case t: Throwable => error("Error parsing JSON message from browser.", t)
      }
    }
  }

  // Default the connection to be held by ConnectionHolder
  ConnectionHolder.hold(this)
}