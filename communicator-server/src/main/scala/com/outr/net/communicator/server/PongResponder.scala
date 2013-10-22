package com.outr.net.communicator.server

/**
 * @author Matt Hicks <matt@outr.com>
 */
object PongResponder extends Receiver {
  def receive(message: Message, connection: Connection) = if (message.event == "ping") {
    connection.send("pong", data = message.data)
  }
}
