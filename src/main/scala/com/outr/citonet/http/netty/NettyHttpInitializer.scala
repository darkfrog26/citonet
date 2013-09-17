package com.outr.citonet.http.netty

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpContentCompressor, HttpResponseEncoder, HttpRequestDecoder}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class NettyHttpInitializer extends ChannelInitializer[SocketChannel] {
  def initChannel(channel: SocketChannel) = {
    val pipeline = channel.pipeline()

    pipeline.addLast("decoder", new HttpRequestDecoder());
    // Uncomment the following line if you don't want to handle HttpChunks.
    //p.addLast("aggregator", new HttpObjectAggregator(1048576));
    pipeline.addLast("encoder", new HttpResponseEncoder());
    pipeline.addLast("deflater", new HttpContentCompressor());
    pipeline.addLast("handler", new HttpSnoopServerHandler());
  }
}
