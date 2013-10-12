package com.outr

/**
 * @author Matt Hicks <matt@outr.com>
 */
package object net {
  implicit def javaURL2URL(url: java.net.URL) = URL(url)
  implicit def tuple2HostAndPort(t: (String, Int)) = HostAndPort(t._1, t._2)
  implicit def port2HostAndPort(port: Int) = HostAndPort(null, port)
}
