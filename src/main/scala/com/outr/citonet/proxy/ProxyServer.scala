package com.outr.citonet.proxy

import java.net.{SocketTimeoutException, InetSocketAddress, ServerSocket}
import org.powerscala.concurrent.{Time, Pool}
import scala.annotation.tailrec
import org.powerscala.log.Logging
import com.outr.citonet.{URL, Protocol}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyServer(val port: Int,
                  val hostname: String = null,
                  val backlog: Int = 0,
                  val timeout: Int = 1000,
                  val receiveBufferSize: Int = 1024) extends Logging {
  private val serverSocket = new ServerSocket()
  protected[proxy] val buffers = Pool[Array[Byte]](new Array[Byte](receiveBufferSize))
  private var connections = Set.empty[ProxyConnection]
  private var keepAlive = true
  private val listenThread = new Thread {
    override def run() {
      listen()
    }
  }
  private var mappings = Map.empty[String, ProxyMapping]
  private var default: String = null

  open()
  listenThread.start()

  private def open() = {
    val address = if (hostname == null) {
      new InetSocketAddress(port)
    } else {
      new InetSocketAddress(hostname, port)
    }
    serverSocket.setReceiveBufferSize(receiveBufferSize)
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

  def addMapping(mapping: ProxyMapping, default: Boolean = false) = synchronized {
    mappings += mapping.domain -> mapping
    if (default) {
      this.default = mapping.domain
    }
  }

  def removeMapping(mapping: ProxyMapping) = synchronized {
    mappings -= mapping.domain
  }

  def getMapping(url: URL): Option[ProxyMapping] = {
    val mapping = mappings.get(url.domain)
    if (mapping.nonEmpty) {
      mapping
    } else if (default != null) {
      mappings.get(default)
    } else {
      None
    }
  }

  def shutdown(waitForConnections: Double = Double.MaxValue) = {
    keepAlive = false
    Time.waitFor(waitForConnections) {
      connections.isEmpty
    }
  }
}

object ProxyServer {
  def main(args: Array[String]): Unit = {
    val server = new ProxyServer(port = 8888)
    server.addMapping(ProxyMapping("outr.com", URL.parse("http://www.projectspeaker.com").get), default = true)
  }
}