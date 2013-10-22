package com.outr.net.communicator.server

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Receiver {
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
      case message => receive(message, connection)
    }
  }

  def receive(message: Message, connection: Connection): Unit
}
