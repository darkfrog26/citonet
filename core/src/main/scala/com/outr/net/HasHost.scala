package com.outr.net

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HasHost {
  def host: String

  lazy val ip: Option[(Int, Int, Int, Int)] = host match {
    case URL.IpAddressRegex(a, b, c, d) => Some((a.toInt, b.toInt, c.toInt, d.toInt))
    case _ => None
  }

  lazy val isIp = ip.nonEmpty

  lazy val domain = isIp match {
    case true => host
    case false => {
      val parts = host.split('.')
      if (parts.length > 1) {
        s"${parts(parts.length - 2)}.${parts(parts.length - 1)}"
      } else {
        host
      }
    }
  }
}