package com.outr.net.proxy

import java.io._
import java.net.{ServerSocket, Socket}
import java.util.concurrent.ConcurrentLinkedQueue

import com.outr.net.{HostAndPort, URL}
import org.powerscala.concurrent.Executor

/**
 * @author Matt Hicks <matt@outr.com>
 */
class SimpleProxy {

}

object SimpleProxy {
  val HeadingRegex = """([A-Z]+) (.+) HTTP/1.1""".r
  val HostRegex = """Host: (.+)""".r

  def main(args: Array[String]): Unit = {
    val server = new ServerSocket(8123)
    while (true) {
      val socket = server.accept()
      if (socket != null) {
        new SimpleProxyConnection(socket, (url: URL) => HostAndPort("captiveimagination.com", 80))
      }
    }
  }
}

class SimpleProxyConnection(socket: Socket, proxy: URL => HostAndPort) {
  import com.outr.net.proxy.SimpleProxy._

  val input = socket.getInputStream
  val output = socket.getOutputStream

  private var remoteSocket: Socket = _
  private var remoteInput: InputStream = _
  private var remoteOutput: OutputStream = _

  private var keepAlive = true
  private var host: String = null
  private var uri: String = null
  private var url: URL = null
  private val readQueue = new ConcurrentLinkedQueue[String]()

  Executor.invoke {
    enqueue()
  }

  // Enqueues the incoming read lines
  private def enqueue(): Unit = if (!keepAlive) {
    // Nothing left to do
  } else {
    val line = reader.readLine()
    if (line == null) {
      disconnect()
    } else {
      println(s"Queuing $line")
      readQueue.add(line)
      line match {
        case HeadingRegex(method, uri) => this.uri = uri
        case HostRegex(host) => {
          this.host = host
          connect()
        }
        case _ => // Ignore
      }
      enqueue()
    }
  }

  private var writeFlushed = false

  // Writes the queued lines to the remote
  private def write(): Unit = if (!keepAlive) {
    // Nothing left to do
  } else {
    Option(readQueue.poll()) match {
      case Some(line) => {
        println(s"Writing $line")
        remoteWriter.write(s"$line\r\n")
        writeFlushed = false
      }
      case None => if (!writeFlushed) {
        remoteWriter.flush()
      } else {
        Thread.sleep(10)
      }
    }
    write()
  }

  private def read(): Unit = if (!keepAlive) {
    // Nothing left to do
  } else {
    val line = remoteReader.readLine()
    if (line == null) {
      disconnect()
    } else {
      println(s"Reading $line")
      writer.write(s"$line\r\n")
      writer.flush()
    }
    read()
  }

  def connect() = {
    url = URL(s"http://$host$uri")
    println(url)
    val hostAndPort = proxy(url)
    println(hostAndPort)
    remoteSocket = new Socket(hostAndPort.host, hostAndPort.port)
    println("Connected!")
    remoteReader = new BufferedReader(new InputStreamReader(remoteSocket.getInputStream))
    remoteWriter = new BufferedWriter(new OutputStreamWriter(remoteSocket.getOutputStream))
    Executor.invoke {
      write()
    }
    Executor.invoke {
      read()
    }
  }

  def disconnect() = {
    new RuntimeException("Disconnected!").printStackTrace()

    keepAlive = false
    if (socket.isConnected) {
      writer.flush()
    }
    reader.close()
    writer.close()
    socket.close()

    if (remoteSocket != null) {
      remoteReader.close()
      remoteWriter.close()
      remoteSocket.close()
    }
  }
}