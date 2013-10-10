package com.outr.net

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait HasHostAndPort extends HasHost with HasPort {
  lazy val hostPort = {
    val b = new StringBuilder
    b.append(host)
    if (port != 80) {
      b.append(':')
      b.append(port)
    }
    b.toString()
  }
}
