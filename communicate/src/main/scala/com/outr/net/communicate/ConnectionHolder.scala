package com.outr.net.communicate

import org.powerscala.LocalStack
import org.powerscala.event.Listenable
import org.powerscala.event.processor.UnitProcessor
import org.powerscala.json._

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait ConnectionHolder extends Listenable {
  private var _connections = Set.empty[Connection]
  def connections = _connections

  val addedConnection = new UnitProcessor[ConnectionAdded]("connected")
  val removedConnection = new UnitProcessor[ConnectionRemoved]("connected")
  val connected = new UnitProcessor[Connection]("connected")
  val textEvent = new UnitProcessor[TextMessage]("text")
  val binaryEvent = new UnitProcessor[BinaryMessage]("binary")
  val jsonEvent = new UnitProcessor[Any]("json")
  val errorEvent = new UnitProcessor[ErrorMessage]("error")
  val disconnectedEvent = new UnitProcessor[DisconnectedMessage]("disconnected")

  addedConnection.on {                              // Add the connection to the set
    case evt => synchronized {
      _connections += evt.connection
    }
  }

  disconnectedEvent.on {                       // Remove the holder after disconnect
    case evt => evt.connection.holder := null
  }

  removedConnection.on {                            // Remove the connection from the set
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

object ConnectionHolder extends ConnectionHolder {
  val stack = new LocalStack[Connection]

  def connection = stack()
}