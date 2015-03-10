package com.outr.net

import java.net.{URLDecoder, URLEncoder}
import java.net
import org.powerscala.IO
import com.outr.net.http.HttpParameters

import scala.annotation.tailrec

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class URL(protocol: Protocol = Protocol.Http,
               host: String = "localhost",
               port: Int = 80,
               ip: IP = IP.LocalHost,
               path: String = "/",
               parameters: HttpParameters = HttpParameters.Empty,
               hash: String = null,
               isEncoded: Boolean = true) extends HasHostAndPort with HasIP {
  lazy val base = if (protocol != null) {
    if (host != null) {
      s"$protocol://$hostPort"
    } else {
      s"$protocol:"
    }
  } else {
    hostPort
  }

  lazy val baseAndPath = s"$base$path"

  lazy val filename = path.substring(path.lastIndexOf('/') + 1)

  lazy val extension = if (filename.indexOf('.') != -1) {
    filename.substring(filename.lastIndexOf('.') + 1).toString
  } else {
    null
  }

  lazy val javaURL = new net.URL(encoded.toString)

  /**
   * Contacts the supplied URL and returns the response as a String.
   */
  def loadAsString() = IO.copy(javaURL)

  private lazy val s = {
    val b = new StringBuilder(baseAndPath)
    if (parameters.values.nonEmpty) {
      b.append(parameters.values.map {
        case (key, values) => values.map(value => s"$key=$value")
      }.flatten.mkString("?", "&", ""))
    }
    if (hash != null && hash.nonEmpty) {
      b.append('#')
      b.append(hash)
    }
    b.toString()
  }

  def breakDown = s"protocol: $protocol, host: $host, port: $port, path: $path, parameters: $parameters, hash: $hash"

  def param(key: String, value: String) = {
    copy(parameters = (parameters + (key -> value)).copy(isEncoded = isEncoded))
  }

  override def toString = s

  private def encode(s: String) = URLEncoder.encode(s, "UTF-8")
  private def decode(s: String) = URLDecoder.decode(s, "UTF-8")

  def encoded = if (isEncoded) {
    this
  } else {
    val encodedPath = path.split("/").map(encode).mkString("/")
    copy(path = encodedPath, parameters = parameters.encoded, isEncoded = true)
  }

  def decoded = if (isEncoded) {
    copy(path = URLDecoder.decode(path, "UTF-8"), parameters = parameters.decoded, isEncoded = false)
  } else {
    this
  }
}

object URL {
  val URLParser = """(\p{Alpha}+://)([\p{Alnum}-.]*):?(\d*)(/.*?)?([?].*)?""".r
  val URLParser2 = """([a-zA-Z0-9:]+:)(/.+)([?].*)?""".r
  val URLParser3 = """([\p{Alnum}-.]*):?(\d*)(/.*?)?([?].*)?""".r

  def encoded(url: String): URL = parse(url, encoded = true).getOrElse(throw new RuntimeException(s"Unable to parse URL: $url"))
  def decoded(url: String): URL = parse(url, encoded = false).getOrElse(throw new RuntimeException(s"Unable to parse URL: $url"))

  def apply(url: net.URL): URL = if (url != null) {
    parse(url.toString, encoded = true).getOrElse(throw new RuntimeException(s"Unable to parse java.net.URL($url) to com.outr.net.URL."))
  } else {
    null
  }

  def lookupResource(s: String) = apply(Thread.currentThread().getContextClassLoader.getResource(s))

  @tailrec
  final def parsePath(path: String): String = {
    val dotDotIndex = path.indexOf("/../")
    if (dotDotIndex == -1) {
      path
    } else {
      val before = path.substring(0, dotDotIndex)
      val after = path.substring(dotDotIndex + 4)
      val adjustIndex = before.lastIndexOf('/')
      val modifiedPath = if (adjustIndex != -1) {
        s"${before.substring(0, adjustIndex)}/$after"
      } else {
        s"$before/$after"
      }
      parsePath(modifiedPath)
    }
  }

  def parse(url: String, encoded: Boolean) = url match {
    case URLParser(_protocol, host, _port, _path, _parameters) => {
      val protocol = Protocol.byScheme(_protocol.substring(0, _protocol.lastIndexOf(':')))
      val port = _port match {
        case "" => 80
        case p => p.toInt
      }
      val path = _path match {
        case null => "/"
        case p => parsePath(p)
      }
      val parameters = HttpParameters.parse(_parameters, encoded)
      Some(URL(protocol = protocol, host = host, port = port, path = path, parameters = parameters, isEncoded = encoded))
    }
    case URLParser2(_protocol, _path, _parameters) => {
      val protocol = _protocol match {
        case null => Protocol.Http
        case p => Protocol.byScheme(p.substring(0, p.lastIndexOf(':')))
      }
      val path = _path match {
        case null => "/"
        case p => parsePath(p)
      }
      val parameters = HttpParameters.parse(_parameters, encoded)
      Some(URL(protocol = protocol, host = null, port = -1, path = path, parameters = parameters, isEncoded = encoded))
    }
    case URLParser3(host, _port, _path, _parameters) => {
      val protocol = Protocol.Http
      val port = _port match {
        case "" => 80
        case p => p.toInt
      }
      val path = _path match {
        case null => "/"
        case p => parsePath(p)
      }
      val parameters = HttpParameters.parse(_parameters, encoded)
      Some(URL(protocol = protocol, host = host, port = port, path = path, parameters = parameters, isEncoded = encoded))
    }
    case _ => None
  }
}
