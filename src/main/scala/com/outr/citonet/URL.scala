package com.outr.citonet

import java.net.{URLEncoder, URLDecoder}

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class URL(method: Method = Method.Get,
               protocol: Protocol = Protocol.Http,
               host: String = "localhost",
               port: Int = 80,
               path: String = "/",
               parameters: Map[String, List[String]] = Map.empty,
               hash: String = null) {
  lazy val ip: Option[(Int, Int, Int, Int)] = host match {
    case URL.IpAddressRegex(a, b, c, d) => Some((a.toInt, b.toInt, c.toInt, d.toInt))
    case _ => None
  }

  lazy val isIp = ip.nonEmpty

  lazy val domain = isIp match {
    case true => host
    case false => {
      val split = host.split('.')
      if (split.length > 1) {
        "%s.%s".format(split(split.length - 2), split(split.length - 1))
      } else {
        host
      }
    }
  }

  lazy val hostPort = {
    val b = new StringBuilder
    b.append(host)
    if (port != 80) {
      b.append(':')
      b.append(port)
    }
    b.toString()
  }

  lazy val base = {
    val b = new StringBuilder
    b.append(protocol)
    b.append("://")
    b.append(hostPort)
    b.toString()
  }

  lazy val baseAndPath = "%s%s".format(base, path)

  private lazy val s = {
    val b = new StringBuilder(baseAndPath)
    if (parameters.nonEmpty) {
      b.append(parameters.map {
        case (key, values) => values.map(value => "%s=%s".format(URLEncoder.encode(key, "utf-8"), URLEncoder.encode(value, "utf-8")))
      }.flatten.mkString("?", "&", ""))
    }
    if (hash != null && hash.nonEmpty) {
      b.append('#')
      b.append(hash)
    }
    b.toString()
  }

  def breakDown = s"method = $method, protocol: $protocol, host: $host, port: $port, path: $path, parameters: $parameters, hash: $hash"

  override def toString = s
}

object URL {
  val IpAddressRegex = """\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b""".r
  val Parser = """(\p{Alpha}+://)?([\p{Alnum}-.]*):?(\d*)(/.*?)?([?].*)?""".r

  def parse(url: String) = url match {
    case Parser(_protocol, host, _port, _path, _parameters) => {
      val protocol = _protocol match {
        case null => Protocol.Http
        case p => Protocol.byScheme(p.substring(0, p.length - 3))
      }
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
      Some(URL(protocol = protocol, host = host, port = port, path = path, parameters = parameters))
    }
    case _ => None
  }
}
