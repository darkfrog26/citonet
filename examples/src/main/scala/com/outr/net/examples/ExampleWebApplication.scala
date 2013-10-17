package com.outr.net.examples

import com.outr.net.http.WebApplication
import com.outr.net.communicator.server.{PongResponder, Communicator}
import org.powerscala.log.Logging
import com.outr.net.http.session.MapSession
import com.outr.net.http.request.HttpRequest

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ExampleWebApplication extends WebApplication[MapSession] with Logging {
  PongResponder.connect()     // Support ping-pong

  protected def createSession(request: HttpRequest, id: String) = new MapSession(id)

  def init() = {
    Communicator.configure(this)

    // Add example html files
    addClassPath("/", "html/")

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

  override def dispose() = {
    super.dispose()
    info("Disposed application!")
  }
}
