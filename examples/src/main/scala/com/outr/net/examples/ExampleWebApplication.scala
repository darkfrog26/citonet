package com.outr.net.examples

import com.outr.net.communicate.ConnectionHolder
import com.outr.net.http.{HttpHandler, WebApplication}
import com.outr.net.http.tomcat.TomcatApplication
import com.outr.net.service.Service
import org.powerscala.json.{TypedSupport, CaseClassSupport}
import org.powerscala.log.Logging
import com.outr.net.http.session.MapSession
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.handler.{MultipartSupport, MultipartHandler, CachedHandler}
import com.outr.net.http.jetty.JettyApplication
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import java.io.File
import com.outr.net.http.content.StringContent
import com.outr.net.proxy.{HostProxy, ProxyHandler}
import org.powerscala.Priority

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ExampleWebApplication extends WebApplication[MapSession] with Logging with JettyApplication {
  protected def createSession(request: HttpRequest, id: String) = new MapSession(id, this)

  def init() = {
    handlers += CachedHandler     // Add caching support

    // Proxy anything coming into the hostname "testing"
    handlers += ProxyHandler(HostProxy("testing", "outr.com", destinationPort = Some(80)), priority = Priority.Critical)

    addHandler(new MultipartHandler with MultipartSupport {
      def create(request: HttpRequest, response: HttpResponse) = this

      def begin(request: HttpRequest, response: HttpResponse) = println(s"Beginning multipart processing...")

      def onField(name: String, value: String) = println(s"onField! $name = $value")

      def onFile(filename: String, file: File) = println(s"onFile! $filename - ${file.length()}")

      def finish(request: HttpRequest, response: HttpResponse) = {
        response.copy(status = HttpResponseStatus.OK, content = StringContent("Received files!"))
      }
    }, "/uploader")

    // Add example html files
    addClassPath("/", "html/")

    register("/communicate.js", "communicate.js")

    TypedSupport.register("test", classOf[Test])

    // Reverse all WebSocket messages received and send back to the browser
    ConnectionHolder.textEvent.on {
      case evt => if (evt.message != "Ping") {
        ConnectionHolder.broadcast(evt.message.reverse)
      }
    }
    ConnectionHolder.addedConnection.on {
      case evt => println(s"Connection added! ${ConnectionHolder.connections.size}")
    }
    ConnectionHolder.removedConnection.on {
      case evt => println(s"Connection removed! ${ConnectionHolder.connections.size}")
    }
    ConnectionHolder.jsonEvent.on {
      case evt => {
        println(s"Received: $evt")
        ConnectionHolder.broadcastJSON(Test("Awesome!"))
      }
    }

    // Service
    val service = Service[Receiving, Sending] {
      case receiving => {
        println(s"Receiving: $receiving")
        Sending(receiving.name.reverse)
      }
    }
    service.bindTo(this, "/reverse")
  }

  override def dispose() = {
    super.dispose()
    info("Disposed application!")
  }
}

case class Receiving(name: String)

case class Sending(name: String)

case class Test(text: String)