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
  val text = new UnitProcessor[TextMessage]("text")
  val binary = new UnitProcessor[BinaryMessage]("binary")
  val json = new UnitProcessor[Any]("json")
  val error = new UnitProcessor[ErrorMessage]("error")
  val disconnected = new UnitProcessor[DisconnectedMessage]("disconnected")

  def send(message: String): Unit

  def sendJSON(message: Any) = {
    val s = toJSON(message).compact
    send(s)
  }

  holder.change.on {
    case evt => {
      if (evt.oldValue != null) {
        evt.oldValue.removed.fire(ConnectionRemoved(this))
      }
      if (evt.newValue != null) {
        evt.newValue.added.fire(ConnectionAdded(evt.oldValue, this))
      }
    }
  }

  connected.on(holder().connected.fire(_))
  text.on(holder().text.fire(_))
  binary.on(holder().binary.fire(_))
  json.on(holder().json.fire(_))
  error.on(holder().error.fire(_))
  disconnected.on(holder().disconnected.fire(_))

  text.on {
    case evt => if (evt.message == "Ping") {              // Default Ping / Pong support
      send("Pong")
    } else if (evt.message.startsWith("::json::")) {      // JSON support
      try {
        ConnectionHolder.stack.context(this) {
          val json = evt.message.substring(8)
          val obj = fromJSON(json)
          this.json.fire(obj)
        }
      } catch {
        case t: Throwable => error("Error parsing JSON message from browser.", t)
      }
    }
  }

  // Default the connection to be held by ConnectionHolder
  ConnectionHolder.hold(this)
}