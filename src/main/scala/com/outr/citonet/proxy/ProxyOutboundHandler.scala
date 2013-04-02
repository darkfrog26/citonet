package com.outr.citonet.proxy

import io.netty.channel._
import io.netty.handler.codec.http.HttpObject
import io.netty.buffer.BufUtil
import com.outr.citonet.proxy.ProxyServer._

/**
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
class ProxyOutboundHandler(channel: Channel, webSocket: Boolean) extends ChannelInboundMessageHandlerAdapter[HttpObject] {
  def messageReceived(ctx: ChannelHandlerContext, msg: HttpObject) {
    // Add the received response to the outbound buffer
    channel.write(BufUtil.retain(msg)).addListener(new ChannelFutureListener {
      def operationComplete(future: ChannelFuture) {
        ctx.channel.read()

        // This is only ok for non "keep-alive"
        if (!webSocket) {
          closeOnFlush(future.channel())
        }
      }
    })
  }

  override def endMessageReceived(ctx: ChannelHandlerContext) {
    // Flush now
    channel.flush().addListener(new ChannelFutureListener {
      def operationComplete(future: ChannelFuture) {
        if (future.isSuccess) {
          ctx.channel.read()
        } else {
          future.channel.close()
        }
      }
    })
  }

  override def channelInactive(context: ChannelHandlerContext) = {
    closeOnFlush(channel)
  }

  override def exceptionCaught(context: ChannelHandlerContext, cause: Throwable) = {
    cause.printStackTrace()
    closeOnFlush(context.channel())
  }
}
