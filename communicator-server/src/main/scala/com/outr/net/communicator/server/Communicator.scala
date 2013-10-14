package com.outr.net.communicator.server

import com.outr.net.http.HttpHandler
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse
import com.outr.net.http.content.{InputStreamContent, StringContent}

import org.powerscala.json._
import org.powerscala.IO
import scala.util.parsing.json.JSON
import org.powerscala.log.Logging
import org.powerscala.event.processor.UnitProcessor
import scala.annotation.tailrec
import org.powerscala.event.Listenable
import org.powerscala.concurrent.Time._
import org.powerscala.concurrent.Time
import org.powerscala.property.Property

/**
 * Communicator represents the message handling functionality for real-time communication between client and server.
 *
 * @author Matt Hicks <matt@outr.com>
 */
object Communicator extends HttpHandler with Logging with Listenable {
  private var connections = Map.empty[String, Connection]

  /**
   * Amount of time to wait during polling for a connection to exist since connections are created by the "send" aspect
   * there may be a certain amount of time between polling connects and the connection is created by "send".
   *
   * Defaults to 10 seconds
   */
  val waitForConnectionTime = Property[Double](default = Some(10.seconds))
  /**
   * Amount of time for polling to wait for messages to appear in the queue before returning an empty response.
   *
   * Defaults to 10 seconds
   */
  val waitForDataTime = Property[Double](default = Some(10.seconds))

  val created = new UnitProcessor[Connection]("created")
  val connected = new UnitProcessor[Connection]("connected")
  val disconnected = new UnitProcessor[Connection]("disconnected")
  val disposed = new UnitProcessor[Connection]("disposed")

  def onReceive(request: HttpRequest) = {
    val data = request.content match {
      case Some(content) => content match {
        case isc: InputStreamContent => JSON.parseFull(IO.copy(isc.input)).get.asInstanceOf[Map[String, Any]]
      }
      case None => null
    }
    val id = data("id").asInstanceOf[String]
    val messageType = data("type").asInstanceOf[String]
//    println(s"Communication received: $request - Data: $data")

    val json = try {
      messageType match {
        case "receive" => receive(request, id, data("lastReceiveId").asInstanceOf[Double].toInt)
        case "send" => {
          val messages = data("messages").asInstanceOf[List[Map[String, Any]]].map {
            case entry => JSONConverter.parseJSON[Message](entry)
          }
          send(request, id, messages)
        }
      }
    } catch {
      case exc: MessageException => {
        exc.printStackTrace()

        generate(Response(status = false, failure = exc.failure), specifyClassName = false)
      }
    }
    HttpResponse(StringContent(json, "application/json"))
  }

  private def receive(request: HttpRequest, id: String, lastReceiveId: Int) = {
//    info(s"Receive: $id, $lastReceiveId, $request")

    Time.waitFor(waitForConnectionTime()) {    // Wait for a short period for the connection to be created if it doesn't exist
      connections.contains(id)
    }
    val connection = connections.getOrElse(id, throw new MessageException(s"Unable to find connection for receive by id: $id", MessageReceiveFailure.ConnectionNotFound))
    Time.waitFor(waitForDataTime()) {          // Wait for a message to be ready to send to the client
      connection.hasMessage
    }

    val messages = connection.messages(lastReceiveId)
    generate(Response(status = true, data = messages), specifyClassName = false)
  }

  // Handles "send" coming from client
  private def send(request: HttpRequest, id: String, messages: List[Message]) = {
    info(s"Send: $id, $messages")

    processMessages(id, messages)

    generate(Response(status = true), specifyClassName = false)
  }

  @tailrec
  private def processMessages(id: String, messages: List[Message], _connection: Connection = null): Unit = {
    if (messages.nonEmpty) {
      var connection = _connection

      val message = messages.head
      if (message.id == -1) {
        message.event match {
          case "create" => create(id)
          case "connect" => connect(id)
        }
      } else {
        connections.get(id) match {
          case Some(c) => {
            connection = c
            connection.receive(message)
          }
          case None => throw new MessageException(s"Connection not found for $id and message: $message.", MessageReceiveFailure.ConnectionNotFound)
        }
      }

      processMessages(id, messages.tail, connection)
    }
  }

  private def create(id: String) = synchronized {
    connections.get(id) match {
      case Some(connection) => throw new MessageException(s"Connection already exists ($id)!", MessageReceiveFailure.ConnectionAlreadyExists)
      case None => {
        val connection = new Connection(id)
        connections += id -> connection
        created.fire(connection)
      }
    }
  }

  private def connect(id: String) = {
    connections.get(id) match {
      case Some(connection) => {
        connection.send("connected", null, highPriority = true)
        connected.fire(connection)
      }
      case None => throw new MessageException(s"Connection not found for $id during connect!", MessageReceiveFailure.ConnectionNotFound)
    }
  }
}

trait MessageReceiveFailure {
  def error: Int
}

object MessageReceiveFailure {
  val ConnectionNotFound = GeneralMessageReceiveFailure(MessageReceiveFailureCode.ConnectionNotFound)
  val ConnectionAlreadyExists = GeneralMessageReceiveFailure(MessageReceiveFailureCode.ConnectionAlreadyExists)
  val InvalidMessageId = GeneralMessageReceiveFailure(MessageReceiveFailureCode.InvalidMessageId)
}

object MessageReceiveFailureCode {
  val ConnectionNotFound = 1
  val ConnectionAlreadyExists = 2
  val InvalidMessageId = 3
}

case class GeneralMessageReceiveFailure(error: Int) extends MessageReceiveFailure

case class Response(status: Boolean, data: List[Message] = Nil, failure: MessageReceiveFailure = null)