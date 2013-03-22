package com.outr.citonet.proxy

import io.netty.channel.{ChannelHandlerContext, ChannelInboundMessageHandlerAdapter, Channel}
import io.netty.handler.codec.http.HttpRequest

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyOutboundHandler(inboundChannel: Channel) extends ChannelInboundMessageHandlerAdapter[HttpRequest] {
  def messageReceived(context: ChannelHandlerContext, message: HttpRequest) = {
    println("OutboundMessageReceived: %s".format(message))
  }
}
