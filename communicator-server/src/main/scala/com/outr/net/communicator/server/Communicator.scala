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

/**
 * Communicator represents the message handling functionality for real-time communication between client and server.
 *
 * @author Matt Hicks <matt@outr.com>
 */
object Communicator extends HttpHandler with Logging with Listenable {
  private var connections = Map.empty[String, Connection]

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
    println(s"Communication received: $request - Data: $data")
    messageType match {
      case "receive" => receive(request, id, data("lastReceiveId").asInstanceOf[Double].toInt)
      case "send" => {
        val messages = data("messages").asInstanceOf[List[Map[String, Any]]].map {
          case entry => JSONConverter.parseJSON[Message](entry)
        }
        send(request, id, messages)
      }
    }
  }

  private def receive(request: HttpRequest, id: String, lastReceiveId: Int) = {
    info(s"Receive: $id, $lastReceiveId, $request")

    // TODO: wait for a period of time for connection to be created if it doesn't yet exist before erroring

    // TODO: wait for content to be received or pull from queue

    // TODO: remove this
    val response = List(Message(1, "Hello World"), Message(2, "Goodbye World"))

    val json = generate(response, specifyClassName = false)
    HttpResponse(StringContent(json, "application/json"))
  }

  // Handles "send" coming from client
  private def send(request: HttpRequest, id: String, messages: List[Message]) = {
    info(s"Send: $id, $messages")

    val json = try {
      processMessages(id, messages)

      generate(Response(status = true), specifyClassName = false)
    } catch {
      case exc: MessageException => {
        exc.printStackTrace()

        generate(Response(status = false, failure = exc.failure), specifyClassName = false)
      }
    }
    HttpResponse(StringContent(json, "application/json"))
  }

  @tailrec
  private def processMessages(id: String, messages: List[Message], _connection: Connection = null): Unit = {
    if (messages.nonEmpty) {
      var connection = _connection

      val message = messages.head
      if (message.id == -1) {
        message.data match {
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
      case Some(connection) => connected.fire(connection)
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
}

object MessageReceiveFailureCode {
  val ConnectionNotFound = 1
  val ConnectionAlreadyExists = 2
}

case class GeneralMessageReceiveFailure(error: Int) extends MessageReceiveFailure

case class Response(status: Boolean, failure: MessageReceiveFailure = null)