package com.outr.net.examples

import com.outr.net._
import com.outr.net.http.WebApplication
import com.outr.net.communicator.server.{PongResponder, Communicator}
import com.outr.net.http.content.URLContent
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ExampleWebApplication extends WebApplication with Logging {
  PongResponder.connect()     // Support ping-pong

  def init() = {
    addClassPath("/", "html/")
    addContent("/communicator.js", URLContent(getClass.getClassLoader.getResource("communicator.js")))
    addClassPath("/GWTCommunicator/", "GWTCommunicator/")
    addHandler("/Communicator/connect.html", Communicator)

    Communicator.created.on {
      case connection => {
        info(s"Created! ${connection.id}")
        connection.received.on {
          case message => info(s"Message Received: $message")
        }
      }
    }
    Communicator.connected.on {
      case connection => {
        info(s"Connected! ${connection.id}")
      }
    }
    Communicator.disconnected.on {
      case connection => {
        info(s"Disconnected! ${connection.id}")
      }
    }
    Communicator.disposed.on {
      case connection => {
        info(s"Disposed! ${connection.id}")
      }
    }
  }
}
