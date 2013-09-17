package com.outr.citonet.http.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel

/**
 * @author Matt Hicks <matt@outr.com>
 */
class NettyHttpSupport {
  val bootstrap = new ServerBootstrap()
  val parentGroup = new NioEventLoopGroup()
  val childGroup = new NioEventLoopGroup()

  private def init() = {
    bootstrap.group(parentGroup, childGroup)
    bootstrap.channel(classOf[NioServerSocketChannel])
//    bootstrap.childHandler(handler)
  }

  def bind(host: String, port: Int) = if (host != null && host != "") {
    bootstrap.bind(host, port).sync()
  } else {
    bootstrap.bind(port).sync()
  }
}