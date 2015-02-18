package com.outr.net.proxy

import java.io.{OutputStream, ByteArrayOutputStream, ByteArrayInputStream, InputStream}
import java.net.{Socket, InetAddress, ServerSocket}

import com.outr.net.{URL, Method}
import org.powerscala.IO
import org.powerscala.concurrent.Executor

import scala.annotation.tailrec

/**
 * @author Matt Hicks <matt@outr.com>
 */
class FastProxy(host: String = null, port: Int = 8888, backlog: Int = 50) {
  private var server: ServerSocket = _

  def start() = {
    server = new ServerSocket(port, backlog, if (host != null) InetAddress.getByName(host) else null)

    Executor.invoke {
      accept()
    }
  }

  private def accept() = {
    val socket = server.accept()
    socket.setTcpNoDelay(true)
    val stream = new DelayedCrossStream(socket, socket.getInputStream, socket.getOutputStream)
    println(stream.url)
    stream.connect("localhost", 8080)
  }
}

object FastProxy {
  // TODO: figure out how to properly terminate connections
  def main(args: Array[String]): Unit = {
    val proxy = new FastProxy()
    proxy.start()
    Thread.sleep(60000)
  }
}

class DelayedCrossStream(s: Socket, input: InputStream, output: OutputStream) {
  private val MethodRegex = s"""(?i)(${Method.values.map(_.name).mkString("|")}) (/.*) HTTP/1.1""".r
  private val HeaderRegex = """([a-zA-Z0-9-]*?):(.+)""".r

  private val builder = new StringBuilder
  private var method: String = _
  private var path: String = _
  private var headers = List.empty[(String, String)]

  def header(name: String) = headers.find(t => t._1.equalsIgnoreCase(name)).getOrElse(throw new NullPointerException(s"Unable to find header: $name."))._2

  readHeaders()

  def url = URL(s"http://${header("host")}$path")

  @tailrec
  private def readHeaders(): Unit = bufferLine() match {
    case "" => // Finished reading headers
    case MethodRegex(m, p) => {
      method = m
      path = p
      readHeaders()
    }
    case HeaderRegex(key, value) => {
      headers = key.trim -> value.trim :: headers
      readHeaders()
    }
  }

  @tailrec
  private def bufferLine(): String = {
    val b = input.read()
    val c = b.toChar
    if (c == '\n') {
      val s = builder.toString()
      builder.clear()
      s
    } else {
      if (c != '\r') {
        builder.append(c)
      }
      bufferLine()
    }
  }

  def connect(host: String, port: Int) = {
    val socket = new Socket(host, port)
    val in = socket.getInputStream
    val out = socket.getOutputStream
    socket.setTcpNoDelay(true)

    var checkClosed: () => Unit = null
    checkClosed = () => {
      println(s"Closed? ${socket.isClosed} / ${s.isClosed}")
      if (!socket.isClosed) {
        Executor.schedule(1.0)(checkClosed())
      }
    }

    // Stream to remote
    Executor.invoke {
      out.write(s"$method $path HTTP/1.1\r\n".getBytes)
      headers.reverse.foreach {
        case (name, value) => {
          println(s"Writing: $name: $value.")
          out.write(s"$name: $value\r\n".getBytes)
        }
      }
      out.write("\r\n".getBytes)

      IO.stream(input, out)
      println("Finished writing!")
    }

    // Stream from remote
    Executor.invoke {
      IO.stream(in, output)
      println("Finished reading!")
    }

    checkClosed()
  }
}
