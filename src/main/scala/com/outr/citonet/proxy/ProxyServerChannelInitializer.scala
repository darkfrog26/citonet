package com.outr.citonet.proxy

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyServerChannelInitializer extends ChannelInitializer[SocketChannel] {
  def initChannel(ch: SocketChannel) = {
    val pipeline = ch.pipeline()
    pipeline.addLast("codec-http", new HttpServerCodec)
    pipeline.addLast("aggregator", new HttpObjectAggregator(65536))
    pipeline.addLast("handler", new ProxyServerHandler);
  }
}
