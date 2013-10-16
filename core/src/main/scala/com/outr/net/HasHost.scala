package com.outr.net

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HasHost {
  def host: String

  lazy val hostAsIP = IP.get(host)

  lazy val isHostIp = hostAsIP.nonEmpty

  lazy val domain = isHostIp match {
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