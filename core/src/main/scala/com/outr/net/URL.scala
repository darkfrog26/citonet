package com.outr.net

import java.net.{URLDecoder, URLEncoder}
import java.net
import org.powerscala.IO
import com.outr.net.http.HttpParameters

import scala.annotation.tailrec

/**
 * @author Matt Hicks <matt@outr.com>
 *
 * @param raw If supplied, this is the URL in string form. It will not be parsed or used internally, but it will be
 *            returned from toString verbatim. This is intended for situations where the URL was originally in string
 *            form, then parsed to these args. Instead of rebuilding it (and introducing slight RFC compliant but
 *            perhaps still undesired differences) the original string representation can be cached.
 */
case class URL(protocol: Protocol = Protocol.Http,
               host: String = "localhost",
               port: Int = 80,
               ip: IP = IP.LocalHost,
               path: String = "/",
               parameters: HttpParameters = HttpParameters.Empty,
               hash: String = null,
               isEncoded: Boolean = true,
               raw: Option[String] = None) extends HasHostAndPort with HasIP {
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

  lazy val javaURL = new net.URL(toString)

  /**
   * Contacts the supplied URL and returns the response as a String.
   */
  def loadAsString() = IO.copy(javaURL)

  private def rebuild = {
    val b = new StringBuilder(baseAndPath)
    if (parameters.values.nonEmpty) {
      b.append(parameters.values.map {
        case (key, values) => values.map(value => "%s=%s".format(URLEncoder.encode(key, "utf-8"), URLEncoder.encode(value, "utf-8")))
      }.flatten.mkString("?", "&", ""))
    }
    if (hash != null && hash.nonEmpty) {
      b.append('#')
      b.append(hash)
    }
    b.toString()
  }

  private lazy val s = raw.getOrElse(rebuild)

  def breakDown = s"protocol: $protocol, host: $host, port: $port, path: $path, parameters: $parameters, hash: $hash"

  def param(key: String, value: String) = {
    copy(parameters = parameters + (key -> value))
  }

  override def toString = s

  def encoded = if (isEncoded) {
    this
  } else {
    copy(path = URLEncoder.encode(path, "UTF-8"), isEncoded = true)   // TODO: encode more than just path
  }

  def decoded = if (isEncoded) {
    copy(path = URLDecoder.decode(path, "UTF-8"), isEncoded = false)  // TODO: decode more than just path
  } else {
    this
  }
}

object URL {
  val URLParser = """(\p{Alpha}+://)([\p{Alnum}-.]*):?(\d*)(/.*?)?([?].*)?""".r
  val URLParser2 = """([a-zA-Z0-9:]+:)(/.+)([?].*)?""".r
  val URLParser3 = """([\p{Alnum}-.]*):?(\d*)(/.*?)?([?].*)?""".r

  def apply(url: String): URL = parse(url).getOrElse(throw new RuntimeException(s"Unable to parse URL: $url"))

  def apply(url: net.URL): URL = if (url != null) {
    parse(url.toString).getOrElse(throw new RuntimeException(s"Unable to parse java.net.URL($url) to com.outr.net.URL."))
  } else {
    null
  }

  def lookupResource(s: String) = apply(Thread.currentThread().getContextClassLoader.getResource(s))

  @tailrec
  private def parsePath(path: String): String = {
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

  def parse(url: String) = url match {
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
      val parameters = HttpParameters.parse(_parameters)
      Some(URL(protocol = protocol, host = host, port = port, path = path, parameters = parameters, raw = Some(url)))
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
      val parameters = HttpParameters.parse(_parameters)
      Some(URL(protocol = protocol, host = null, port = -1, path = path, parameters = parameters, raw = Some(url)))
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
      val parameters = HttpParameters.parse(_parameters)
      Some(URL(protocol = protocol, host = host, port = port, path = path, parameters = parameters, raw = Some(url)))
    }
    case _ => None
  }
}
