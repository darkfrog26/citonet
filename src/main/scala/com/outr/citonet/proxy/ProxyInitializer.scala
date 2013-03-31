package com.outr.citonet.proxy

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http._

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyInitializer(remoteHost: String, remotePort: Int) extends ChannelInitializer[SocketChannel] {
  def initChannel(channel: SocketChannel) = {
    val p = channel.pipeline()
    p.addLast("decoder", new HttpServerCodec())

    // Only needed if we want to operate on FullHttpRequest / FullHttpResponse only
    p.addLast("aggregator", new HttpObjectAggregator(1048576))

    //p.addLast("deflater", new HttpContentCompressor())
    p.addLast("handler", new ProxyInboundHandler(remoteHost, remotePort))
  }
}
