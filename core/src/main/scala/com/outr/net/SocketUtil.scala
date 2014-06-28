package com.outr.net

import java.io.IOException
import java.net.ServerSocket

import org.powerscala.log.Logging

/**
 * SocketUtil as the name would suggest provides utility methods for sockets.
 *
 * @author Matt Hicks <matt@outr.com>
 */
object SocketUtil extends Logging {
  /**
   * Returns true if the supplied port is not currently in-use for TCP operations.
   */
  def isPortAvailable(port: Int) = try {
    debug(s"Checking port availability: $port")
    val ss = new ServerSocket(port)
    ss.close()
    true
  } catch {
    case exc: IOException => false
  }

  /**
   * Finds the first available port in the supplied sequence.
   */
  def nextAvailablePort(ports: Seq[Int]) = ports.find(port => isPortAvailable(port))

  /**
   * Returns a filtered list of the available ports from the supplied sequence.
   */
  def portScan(ports: Seq[Int]) = ports.filter(isPortAvailable)
}