package com.outr.citonet.proxy

import io.netty.channel._
import io.netty.handler.codec.http.FullHttpResponse

/**
 * @author Matt Hicks <matt@outr.com>
 */
class HttpClientHandler(inbound: Channel) extends SimpleChannelInboundHandler[FullHttpResponse] {
  def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpResponse) = {
    inbound.writeAndFlush(msg.retain(1)).addListener(new ChannelFutureListener {
      def operationComplete(future: ChannelFuture) = {
        future.channel().close()
        inbound.close()
      }
    })
  }
}
