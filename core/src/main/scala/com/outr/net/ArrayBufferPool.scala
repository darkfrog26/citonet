package com.outr.net

import org.powerscala.concurrent.Pool

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ArrayBufferPool extends Pool[Array[Byte]] {
  val BufferSize = 1024

  def initialSize = 0
  def maximumSize = Int.MaxValue

  protected def createItem() = new Array[Byte](BufferSize)

  protected def releaseItem(t: Array[Byte]) = {}

  protected def disposeItem(t: Array[Byte]) = {}
}
