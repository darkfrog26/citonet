package com.outr.citonet.proxy

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelOption, ChannelFutureListener, Channel}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyServer(localPort: Int, remoteHost: String, remotePort: Int) {
  def run() = {
    val bootstrap = new ServerBootstrap()
    try {
      bootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
               .channel(classOf[NioServerSocketChannel])
               .childHandler(new ProxyInitializer(remoteHost, remotePort))
               .childOption(ChannelOption.AUTO_READ.asInstanceOf[ChannelOption[Any]], false)
               .bind(localPort).sync().channel().closeFuture().sync()
    } finally {
      bootstrap.shutdown()
    }
  }
}

object ProxyServer {
  def main(args: Array[String]): Unit = {
    val server = new ProxyServer(8888, "captiveimagination.com", 80)
    server.run()
  }

  def closeOnFlush(channel: Channel) = {
    if (channel.isActive) {
      channel.flush().addListener(ChannelFutureListener.CLOSE)
    }
  }
}