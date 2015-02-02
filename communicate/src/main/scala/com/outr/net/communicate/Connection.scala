package com.outr.net.communicate

import org.powerscala.event.Listenable
import org.powerscala.event.processor.UnitProcessor
import org.powerscala.property.Property

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Connection extends Listenable {
  val holder = Property[ConnectionHolder]()

  val connected = new UnitProcessor[Connection]("connected")
  val text = new UnitProcessor[TextMessage]("text")
  val binary = new UnitProcessor[BinaryMessage]("binary")
  val error = new UnitProcessor[ErrorMessage]("error")
  val disconnected = new UnitProcessor[DisconnectedMessage]("disconnected")

  def send(message: String): Unit

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
  error.on(holder().error.fire(_))
  disconnected.on(holder().disconnected.fire(_))

  // Default Ping / Pong support
  text.on {
    case evt => if (evt.message == "Ping") {
      send("Pong")
    }
  }

  // Default the connection to be held by ConnectionHolder
  ConnectionHolder.hold(this)
}