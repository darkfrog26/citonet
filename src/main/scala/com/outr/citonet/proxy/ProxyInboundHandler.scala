package com.outr.citonet.proxy

import io.netty.channel._
import io.netty.handler.codec.http.HttpRequest
import io.netty.bootstrap.Bootstrap
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.buffer.BufUtil

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyInboundHandler(remoteHost: String, remotePort: Int) extends ChannelInboundMessageHandlerAdapter[HttpRequest] {
  @volatile private var outboundChannel: Channel = _

  def messageReceived(context: ChannelHandlerContext, message: HttpRequest) = {
    println("ProxyInboundHandler.messageReceived")
    val inboundChannel = context.channel()

    BufUtil.retain(message)

    // Attempt remote connection
    val bootstrap = new Bootstrap
    bootstrap.group(inboundChannel.eventLoop())
             .channel(classOf[NioSocketChannel])
             .handler(new ProxyOutboundInitializer(inboundChannel))
    val future = bootstrap.connect(remoteHost, remotePort)
    outboundChannel = future.channel()
    future.addListener(new ChannelFutureListener {
      def operationComplete(future: ChannelFuture) = {
        if (future.isSuccess) {         // Connected - start reading
          println("Read / Write!")
          outboundChannel.write(message)
          inboundChannel.read()
        } else {                        // Failed to connect - close the connection
          println("CLOSE!")
          inboundChannel.close()

          BufUtil.release(message)
        }
      }
    })
  }

  override def endMessageReceived(context: ChannelHandlerContext) = {
    println("endMessageReceived!")
    context.flush()
  }

  override def exceptionCaught(context: ChannelHandlerContext, cause: Throwable) = {
    cause.printStackTrace()
    context.close()
  }
}
