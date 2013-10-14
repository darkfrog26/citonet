package com.outr.net

import java.net.{URLEncoder, URLDecoder}
import java.net
import org.powerscala.IO
import com.outr.net.http.HttpParameters

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class URL(protocol: Protocol = Protocol.Http,
               host: String = "localhost",
               port: Int = 80,
               path: String = "/",
               parameters: HttpParameters = HttpParameters.Empty,
               hash: String = null) extends HasHostAndPort {
  lazy val base = if (host != null) s"$protocol://$hostPort" else s"$protocol:"

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

  private lazy val s = {
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

  def breakDown = s"protocol: $protocol, host: $host, port: $port, path: $path, parameters: $parameters, hash: $hash"

  override def toString = s
}

object URL {
  val IpAddressRegex = """\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b""".r
  val URLParser = """(\p{Alpha}+://)([\p{Alnum}-.]*):?(\d*)(/.*?)?([?].*)?""".r
  val URLParser2 = """([a-zA-Z0-9:]+:)(/.+)([?].*)?""".r
  val URLParser3 = """([\p{Alnum}-.]*):?(\d*)(/.*?)?([?].*)?""".r

  def apply(url: net.URL): URL = if (url != null) {
    parse(url.toString).getOrElse(throw new RuntimeException(s"Unable to parse java.net.URL($url) to com.outr.net.URL."))
  } else {
    null
  }

  def lookupResource(s: String) = apply(Thread.currentThread().getContextClassLoader.getResource(s))

  def parse(url: String) = url match {
    case URLParser(_protocol, host, _port, _path, _parameters) => {
      val protocol = Protocol.byScheme(_protocol.substring(0, _protocol.lastIndexOf(':')))
      val port = _port match {
        case "" => 80
        case p => p.toInt
      }
      val path = _path match {
        case null => "/"
        case p => p
      }
      var parameters = Map.empty[String, List[String]]
      if (_parameters != null && _parameters.length > 1) {
        _parameters.substring(1).split('&').foreach {
          case entry => {
            val split = entry.indexOf('=')
            val (key, value) = if (split == -1) {
              URLDecoder.decode(entry, "utf-8") -> null
            } else {
              URLDecoder.decode(entry.substring(0, split), "utf-8") -> URLDecoder.decode(entry.substring(split + 1), "utf-8")
            }
            val entries = parameters.getOrElse(key, Nil)
            if (value == null) {
              parameters += key -> entries
            } else {
              parameters += key -> (value :: entries.reverse).reverse
            }
          }
        }
      }
      Some(URL(protocol = protocol, host = host, port = port, path = path, parameters = HttpParameters(parameters)))
    }
    case URLParser2(_protocol, _path, _parameters) => {
      val protocol = _protocol match {
        case null => Protocol.Http
        case p => Protocol.byScheme(p.substring(0, p.lastIndexOf(':')))
      }
      val path = _path match {
        case null => "/"
        case p => p
      }
      var parameters = Map.empty[String, List[String]]
      if (_parameters != null && _parameters.length > 1) {
        _parameters.substring(1).split('&').foreach {
          case entry => {
            val split = entry.indexOf('=')
            val (key, value) = if (split == -1) {
              URLDecoder.decode(entry, "utf-8") -> null
            } else {
              URLDecoder.decode(entry.substring(0, split), "utf-8") -> URLDecoder.decode(entry.substring(split + 1), "utf-8")
            }
            val entries = parameters.getOrElse(key, Nil)
            if (value == null) {
              parameters += key -> entries
            } else {
              parameters += key -> (value :: entries.reverse).reverse
            }
          }
        }
      }
      Some(URL(protocol = protocol, host = null, port = -1, path = path, parameters = HttpParameters(parameters)))
    }
    case URLParser3(host, _port, _path, _parameters) => {
      val protocol = Protocol.Http
      val port = _port match {
        case "" => 80
        case p => p.toInt
      }
      val path = _path match {
        case null => "/"
        case p => p
      }
      var parameters = Map.empty[String, List[String]]
      if (_parameters != null && _parameters.length > 1) {
        _parameters.substring(1).split('&').foreach {
          case entry => {
            val split = entry.indexOf('=')
            val (key, value) = if (split == -1) {
              URLDecoder.decode(entry, "utf-8") -> null
            } else {
              URLDecoder.decode(entry.substring(0, split), "utf-8") -> URLDecoder.decode(entry.substring(split + 1), "utf-8")
            }
            val entries = parameters.getOrElse(key, Nil)
            if (value == null) {
              parameters += key -> entries
            } else {
              parameters += key -> (value :: entries.reverse).reverse
            }
          }
        }
      }
      Some(URL(protocol = protocol, host = host, port = port, path = path, parameters = HttpParameters(parameters)))
    }
    case _ => None
  }
}
