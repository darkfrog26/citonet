package com.outr.citonet.proxy

import io.netty.channel.{Channel, ChannelInitializer}
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpContentCompressor, HttpObjectAggregator, HttpResponseEncoder, HttpRequestDecoder}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyOutboundInitializer(inboundChannel: Channel) extends ChannelInitializer[SocketChannel] {
  def initChannel(channel: SocketChannel) = {
    val p = channel.pipeline()
    p.addLast("decoder", new HttpRequestDecoder)
    p.addLast("aggregator", new HttpObjectAggregator(1048576))
    p.addLast("encoder", new HttpResponseEncoder)
    p.addLast("deflater", new HttpContentCompressor())
    p.addLast("handler", new ProxyOutboundHandler(inboundChannel))
  }
}
