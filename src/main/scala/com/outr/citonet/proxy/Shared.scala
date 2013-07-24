package com.outr.citonet.proxy

import io.netty.channel.nio.NioEventLoopGroup
import java.util.concurrent.ConcurrentHashMap
import io.netty.channel.Channel

/**
 * @author Matt Hicks <matt@outr.com>
 */
object Shared {
  val workerGroup = new NioEventLoopGroup
  val webSocketToRemote = new ConcurrentHashMap[Channel, Channel]()
  val remoteToWebSocket = new ConcurrentHashMap[Channel, Channel]()
}
