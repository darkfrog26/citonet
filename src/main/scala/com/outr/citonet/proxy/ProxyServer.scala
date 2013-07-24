package com.outr.citonet.proxy

import io.netty.channel.nio.NioEventLoopGroup
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.Channel

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyServer(val port: Int) {
  val bossGroup = new NioEventLoopGroup
  private var channel: Channel = _

  def start() = {
    channel = createChannel()
  }

  private def createChannel() = {
    val bootstrap = new ServerBootstrap
    bootstrap.group(bossGroup, Shared.workerGroup)
             .channel(classOf[NioServerSocketChannel])
             .childHandler(new ProxyServerChannelInitializer)
    bootstrap.bind(port).sync().channel()
  }
}

object ProxyServer {
  def main(args: Array[String]): Unit = {
    val server = new ProxyServer(3001)
    server.start()
    println("Server started...")
  }
}