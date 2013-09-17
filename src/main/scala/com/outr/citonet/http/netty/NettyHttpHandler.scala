package com.outr.citonet.http.netty

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{HttpRequest => NettyHttpRequest}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class NettyHttpHandler extends SimpleChannelInboundHandler[NettyHttpRequest] {
  def channelRead0(ctx: ChannelHandlerContext, msg: NettyHttpRequest) = {

  }
}
