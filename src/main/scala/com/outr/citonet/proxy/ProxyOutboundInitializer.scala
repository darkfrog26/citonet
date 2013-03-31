package com.outr.citonet.proxy

import io.netty.channel.{Channel, ChannelInitializer}
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http._

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyOutboundInitializer(inboundChannel: Channel) extends ChannelInitializer[SocketChannel] {
  def initChannel(channel: SocketChannel) {
    val p = channel.pipeline()
    p.addLast("encoder", new HttpClientCodec)
    // no need to aggregate as long as we not want to only operate on FullHttpRequest / FullHttpResponse
    p.addLast("aggregator", new HttpObjectAggregator(1048576))
    p.addLast("handler", new ProxyOutboundHandler(inboundChannel))
  }
}
