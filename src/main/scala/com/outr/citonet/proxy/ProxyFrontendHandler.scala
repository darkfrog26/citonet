package com.outr.citonet.proxy

import io.netty.channel._
import io.netty.bootstrap.Bootstrap
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.buffer.ByteBuf
import ProxyServer.closeOnFlush

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyFrontendHandler(remoteHost: String, remotePort: Int) extends ChannelInboundByteHandlerAdapter {
  @volatile private var outboundChannel: Channel = _

  override def channelActive(context: ChannelHandlerContext) = {
    val inboundChannel = context.channel()

    // Attempt connection to remote
    val bootstrap = new Bootstrap()
    bootstrap.group(inboundChannel.eventLoop())
             .channel(classOf[NioSocketChannel])
             .handler(new ProxyBackendHandler(inboundChannel))
             .option(ChannelOption.AUTO_READ.asInstanceOf[ChannelOption[Any]], false)
    val future = bootstrap.connect(remoteHost, remotePort)
    outboundChannel = future.channel()
    future.addListener(new ChannelFutureListener {
      def operationComplete(future: ChannelFuture) = {
        if (future.isSuccess) {   // Connected - start reading
          inboundChannel.read()
        } else {                  // Failed to connect - close the connection
          inboundChannel.close()
        }
      }
    })
  }

  def inboundBufferUpdated(context: ChannelHandlerContext, in: ByteBuf) = {
    val out = outboundChannel.outboundByteBuffer()
    out.writeBytes(in)
    if (outboundChannel.isActive) {
      outboundChannel.flush().addListener(new ChannelFutureListener {
        def operationComplete(future: ChannelFuture) = {
          if (future.isSuccess) {  // Flush successful - start reading the next chunk
            context.channel().read()
          } else {                 // Flush failed - close the connection
            future.channel().close()
          }
        }
      })
    }
  }

  override def channelInactive(context: ChannelHandlerContext) = {
    if (outboundChannel != null) {
      closeOnFlush(outboundChannel)
    }
  }

  override def exceptionCaught(context: ChannelHandlerContext, cause: Throwable) = {
    cause.printStackTrace()
    closeOnFlush(context.channel())
  }
}
