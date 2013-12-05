package com.outr.net.proxy

import java.net.{SocketTimeoutException, InetSocketAddress, ServerSocket}
import org.powerscala.concurrent.Time
import scala.annotation.tailrec
import org.powerscala.log.Logging
import java.io.{FileNotFoundException, File}
import scala.xml.{Elem, XML}
import com.outr.net.{ArrayBufferPool, Protocol, URL}

/**
 * @author Matt Hicks <matt@outr.com>
 */
@deprecated("Use ProxyHandler instead.", "1.0.2")
class ProxyServer(val port: Int,
                  val hostname: String = null,
                  val backlog: Int = 0,
                  val timeout: Int = 1000) extends Logging {
  private val serverSocket = new ServerSocket()
  private var connections = Set.empty[ProxyConnection]
  private var keepAlive = true
  private val listenThread = new Thread {
    override def run() {
      listen()
    }
  }
  val mappings = new ProxyMapping
  var default: URL = null

  open()
  listenThread.start()

  private def open() = {
    val address = if (hostname == null || hostname == "") {
      new InetSocketAddress(port)
    } else {
      new InetSocketAddress(hostname, port)
    }
    serverSocket.setReceiveBufferSize(ArrayBufferPool.BufferSize)
    serverSocket.setReuseAddress(true)
    serverSocket.setSoTimeout(timeout)
    serverSocket.bind(address, backlog)
    info("ProxyServer started. Listening for connections...")
  }

  @tailrec
  private def listen(): Unit = {
    try {
      val socket = serverSocket.accept()
      debug(s"Connection established: $socket")
      val connection = new ProxyConnection(this, socket, Protocol.Http)
      addConnection(connection)
    } catch {
      case exc: SocketTimeoutException => // Ignore timeouts
    }
    if (keepAlive) {
      listen()
    }
  }

  private def addConnection(connection: ProxyConnection) = synchronized {
    connections += connection
  }

  private[proxy] def removeConnection(connection: ProxyConnection) = synchronized {
    connections -= connection
  }

  def getMapping(url: URL): Option[URL] = {
    val proxy = mappings.get(url)
    if (proxy.isEmpty) {
      Option(default)
    } else {
      proxy
    }
  }

  def shutdown(waitForConnections: Double = Double.MaxValue) = {
    keepAlive = false
    Time.waitFor(waitForConnections) {
      connections.isEmpty
    }
  }
}

@deprecated("Use ProxyHandler instead.", "1.0.2")
object ProxyServer extends Logging {
  def load(file: File) = {
    if (!file.exists()) {
      throw new FileNotFoundException(s"File ${file.getAbsolutePath} does not exist!")
    }
    val xml = XML.loadFile(file)

    def attribute(elem: Elem, name: String, default: String) = {
      val value = (elem \ s"@$name").text
      if (value == null || value == "") {
        default
      } else {
        value
      }
    }

    val hostname = attribute(xml, "hostname", null)
    val port = attribute(xml, "port", "8080").toInt
    val backlog = attribute(xml, "backlog", "0").toInt
    val timeout = attribute(xml, "timeout", "1000").toInt
    val receiveBufferSize = attribute(xml, "receiveBufferSize", "1024").toInt
    info(s"Configuration Loaded - Hostname: $hostname, Port: $port, Backlog: $backlog, Timeout: $timeout, Receive Buffer Size: $receiveBufferSize")
    val server = new ProxyServer(port, hostname, backlog, timeout)
    (xml \ "proxy").foreach {
      case elem: Elem => {
        val proxyType = attribute(elem, "type", "host")
        val remoteString = attribute(elem, "remote", null)
        val remote = URL.parse(remoteString).getOrElse(throw new NullPointerException(s"Unable to parse URL: [$remoteString]"))
        proxyType match {
          case "host" => {
            val hostname = attribute(elem, "value", null)
            server.mappings.host(hostname, remote)
          }
          case "hostPort" => {
            val hostname = attribute(elem, "host", null)
            val port = attribute(elem, "port", "80").toInt
            server.mappings.hostPort(hostname, port, remote)
          }
          case "domain" => {
            val domain = attribute(elem, "value", null)
            server.mappings.domain(domain, remote)
          }
          case _ => throw new RuntimeException(s"Unknown proxy type: $proxyType")
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
//    val server = new ProxyServer(port = 8888)
//    val url = URL.parse("http://www.projectspeaker.com").get
//    server.default = url
//    server.mappings.domain("outr.com", url)
    load(new File("proxy.xml"))
  }
}