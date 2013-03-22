package com.outr.citonet.proxy

import io.netty.channel._
import io.netty.buffer.ByteBuf
import ProxyServer.closeOnFlush

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyBackendHandler(inboundChannel: Channel) extends ChannelInboundByteHandlerAdapter {
  override def channelActive(context: ChannelHandlerContext) = {
    context.read()
    context.flush()
  }

  def inboundBufferUpdated(context: ChannelHandlerContext, in: ByteBuf) = {
    val out = inboundChannel.outboundByteBuffer()
    out.writeBytes(in)
    inboundChannel.flush().addListener(new ChannelFutureListener {
      def operationComplete(future: ChannelFuture) = {
        if (future.isSuccess) {
          context.channel.read()
        } else {
          future.channel.close()
        }
      }
    })
  }

  override def channelInactive(context: ChannelHandlerContext) = {
    closeOnFlush(inboundChannel)
  }

  override def exceptionCaught(context: ChannelHandlerContext, cause: Throwable) = {
    cause.printStackTrace()
    closeOnFlush(context.channel())
  }
}
