package com.outr.net.communicator.server

import org.powerscala.event.Listenable
import org.powerscala.event.processor.UnitProcessor
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
class Connection(val id: String) extends Listenable with Logging {
  private var lastReceiveId: Int = 0

  val received = new UnitProcessor[Message]("received")

  // TODO: create queue and sending support

  def receive(message: Message) = synchronized {
    val expectedId = lastReceiveId + 1
    if (message.id != -1 && message.id != expectedId) {
      warn(s"Invalid message id received on server: ${message.id}, but expected: $expectedId")
      false
    } else {
      received.fire(message)
      if (message.id != -1) {
        lastReceiveId = expectedId
      }
    }
  }
}
