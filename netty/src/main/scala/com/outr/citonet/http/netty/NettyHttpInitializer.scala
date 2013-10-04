package com.outr.citonet.http.netty

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpContentCompressor, HttpResponseEncoder, HttpRequestDecoder}
import io.netty.handler.stream.ChunkedWriteHandler

/**
 * @author Matt Hicks <matt@outr.com>
 */
class NettyHttpInitializer(support: NettyHttpSupport) extends ChannelInitializer[SocketChannel] {
  def initChannel(channel: SocketChannel) = {
    val pipeline = channel.pipeline()

    pipeline.addLast("decoder", new HttpRequestDecoder())
    pipeline.addLast("aggregator", new HttpObjectAggregator(Int.MaxValue))
    pipeline.addLast("encoder", new HttpResponseEncoder())
    pipeline.addLast("chunkedWriter", new ChunkedWriteHandler())
    pipeline.addLast("deflater", new HttpContentCompressor())
    pipeline.addLast("handler", new NettyHttpHandler(support))
  }
}
