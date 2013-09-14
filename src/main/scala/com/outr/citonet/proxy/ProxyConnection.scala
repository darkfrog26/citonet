package com.outr.citonet.proxy

import java.net.{SocketException, Socket}
import com.outr.citonet.{Protocol, URL, Method}
import java.io.{InputStream, OutputStream}
import org.powerscala.concurrent.Executor
import scala.annotation.tailrec
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyConnection(server: ProxyServer, socket: Socket, protocol: Protocol) extends Logging {
  private val input = socket.getInputStream
  private val output = socket.getOutputStream
  private val originBuffer = server.buffers()
  private val proxyBuffer = server.buffers()
  private var proxy: Socket = _
  private var proxyInput: InputStream = _
  private var proxyOutput: OutputStream = _
  private var originURL: URL = _
  private var destinationURL: URL = _

  establishProxy()

  protected def establishProxy() = {
    // Read the header from the request
    val len = input.read(originBuffer)
    if (len != -1) {
      val data = new String(originBuffer, 0, len)

      // Assemble the URL
      originURL = ProxyConnection.readURL(data, protocol)

      // Request the proxy address from ProxyServer
      server.getMapping(originURL) match {
        case Some(mapping) => {
          destinationURL = mapping
          debug(s"Proxying $originURL to $destinationURL")

          // Establish proxy connection
          proxy = new Socket(destinationURL.host, destinationURL.port)
          // TODO: support connection timeout
          proxyInput = proxy.getInputStream
          proxyOutput = proxy.getOutputStream

          // Send received buffer
          proxyOutput.write(originBuffer, 0, len)

          // Create cross-communication threads
          Executor.invoke {
            try {
              startReading(input, proxyOutput, originBuffer)
            } catch {
              case t: Throwable => error("Error occurred while transferring inbound -> proxy.", t)
            }
          }
          Executor.invoke {
            try {
              startReading(proxyInput, output, proxyBuffer)
            } catch {
              case t: Throwable => error("Error occurred while transferring proxy -> inbound.", t)
            }
          }
        }
        case None => {
          // TODO: support error page response or redirection
          close()   // TODO: remove this
        }
      }
    } else {
      close()
    }
  }

  private def startReading(input: InputStream, output: OutputStream, buf: Array[Byte]) = {
    try {
      read(input, output, buf)
    } catch {
      case exc: SocketException => close()
    }
  }

  @tailrec
  private def read(input: InputStream, output: OutputStream, buf: Array[Byte]): Unit = {
    val len = input.read(buf)
    if (len == -1) {
      close()
    } else {
      if (len > 0) {
        output.write(buf, 0, len)
        output.flush()
      }
      read(input, output, buf)
    }
  }

  @volatile private var closed = false

  def close() = if (!closed) {
    closed = true
    if (output != null) {
      output.flush()
    }
    if (proxyOutput != null) {
      proxyOutput.flush()
    }

    socket.close()

    // Release the buffers so they can be utilized by another connection
    server.buffers.release(originBuffer)
    server.buffers.release(proxyBuffer)

    // Remove connection from ProxyServer
    server.removeConnection(this)
  }
}

object ProxyConnection {
  private val RequestLineRegexWithParameters = """([a-zA-Z]+) (.*)\?(.+) HTTP/1.1""".r
  private val RequestLineRegex = """([a-zA-Z]+) (.*) HTTP/1.1""".r
  private val HostLineRegex = """Host: (.+?):?(\d*)""".r

  def readURL(data: String, protocol: Protocol) = {
    val headers = data.split("\n").map(s => s.trim).toList
    val requestLine = headers.head
    val hostLine = headers.find(s => s.startsWith("Host:")).getOrElse(throw new NullPointerException(s"Unable to find Host for [$data]"))
    val (method, path, parameters) = readRequestLine(requestLine)
    val (host, port) = readHostLine(hostLine)
    URL(method, protocol, host, port, path, parameters, hash = null)
  }

  private def readRequestLine(line: String) = line match {
    case RequestLineRegexWithParameters(method, path, parameters) => (Method(method), path, parseParameters(parameters))
    case RequestLineRegex(method, path) => (Method(method), path, Map.empty[String, List[String]])
  }

  private def parseParameters(s: String) = {
    s.split("&").map {
      case group => if (group.indexOf('=') != -1) {
        group.substring(0, group.indexOf('=')) -> List(group.substring(group.indexOf('=') + 1))
      } else {
        group -> Nil
      }
    }.toMap
  }

  private def readHostLine(line: String) = line match {
    case HostLineRegex(host, p) => {
      val port = if (p == null || p == "") {
        80
      } else {
        p.toInt
      }
      host -> port
    }
  }
}