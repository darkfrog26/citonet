package com.outr.net.examples

import com.outr.net.communicator.server.{Connection, Message, Receiver}

/**
 * @author Matt Hicks <matt@outr.com>
 */
object TimeResponder extends Receiver {
  def receive(message: Message, connection: Connection) = if (message.event == "time") {
    connection.send("time", Map("millis" -> System.currentTimeMillis(), "nanos" -> System.nanoTime()))
  }
}