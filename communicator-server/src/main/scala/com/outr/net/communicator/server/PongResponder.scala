package com.outr.net.communicator.server

/**
 * @author Matt Hicks <matt@outr.com>
 */
object PongResponder {
  def connect() = {
    Communicator.created.on {
      case (connection, data) => {
        val wrapper = connectTo(connection)
        Communicator.disposed.on {
          case c => if (c eq connection) {
            c.received.remove(wrapper)
          }
        }
      }
    }
  }

  def connectTo(connection: Connection) = {
    connection.received.on {
      case message => if (message.event == "ping") {
        connection.send("pong", data = message.data)
      }
    }
  }
}
