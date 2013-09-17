package com.outr.citonet

import java.net.{URLEncoder, URLDecoder}
import com.outr.citonet.http.HttpParameters
import java.net
import org.powerscala.IO

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class URL(method: Method = Method.Get,
               protocol: Protocol = Protocol.Http,
               host: String = "localhost",
               port: Int = 80,
               path: String = "/",
               parameters: HttpParameters = HttpParameters.Empty,
               hash: String = null) extends HasHostAndPort {
  lazy val base = s"$protocol://$hostPort"

  lazy val baseAndPath = s"$base$path"

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
      Some(URL(protocol = protocol, host = host, port = port, path = path, parameters = HttpParameters(parameters)))
    }
    case _ => None
  }
}
