package com.outr.citonet.http.netty

import io.netty.channel.ChannelInitializer
import java.nio.channels.SocketChannel

/**
 * @author Matt Hicks <matt@outr.com>
 */
class NettyHttpInitializer extends ChannelInitializer[SocketChannel] {
  def initChannel(ch: SocketChannel) = {

  }
}
