package com.outr.net.communicate

import org.powerscala.event.Listenable
import org.powerscala.event.processor.UnitProcessor
import org.powerscala.json._

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait ConnectionHolder extends Listenable {
  private var _connections = Set.empty[Connection]
  def connections = _connections

  val added = new UnitProcessor[ConnectionAdded]("connected")
  val removed = new UnitProcessor[ConnectionRemoved]("connected")
  val connected = new UnitProcessor[Connection]("connected")
  val text = new UnitProcessor[TextMessage]("text")
  val binary = new UnitProcessor[BinaryMessage]("binary")
  val json = new UnitProcessor[Any]("json")
  val error = new UnitProcessor[ErrorMessage]("error")
  val disconnected = new UnitProcessor[DisconnectedMessage]("disconnected")

  added.on {
    case evt => synchronized {
      _connections += evt.connection
    }
  }

  removed.on {
    case evt => synchronized {
      _connections -= evt.connection
    }
  }

  def broadcast(message: String, exclude: Connection*) = {
    val exclusions = exclude.toSet
    connections.foreach {
      case c => if (!exclusions.contains(c)) {
        c.send(message)
      }
    }
  }

  def broadcastJSON(message: Any, exclude: Connection*) = {
    val s = toJSON(message).compact
    broadcast(s"::json::$s", exclude: _*)
  }

  def hold(connection: Connection) = connection.holder := this
}

case class ConnectionAdded(previous: ConnectionHolder, connection: Connection)

case class ConnectionRemoved(connection: Connection)

case class TextMessage(message: String, connection: Connection)

case class BinaryMessage(message: Array[Byte], offset: Int, len: Int, connection: Connection)

case class ErrorMessage(cause: Throwable, connection: Connection)

case class DisconnectedMessage(statusCode: Int, reason: String, connection: Connection)

object ConnectionHolder extends ConnectionHolder