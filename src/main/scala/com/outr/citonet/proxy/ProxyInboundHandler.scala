package com.outr.citonet.proxy

import io.netty.channel._
import io.netty.handler.codec.http.{HttpObject, HttpRequest}
import io.netty.bootstrap.Bootstrap
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.buffer.BufUtil

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyInboundHandler(remoteHost: String, remotePort: Int) extends ChannelInboundMessageHandlerAdapter[HttpObject] {
  private var outboundChannel: Channel = _

  override def channelActive(ctx: ChannelHandlerContext) {
    ctx.read()
  }

  def messageReceived(context: ChannelHandlerContext, message: HttpObject) = {
    val inboundChannel = context.channel()

    BufUtil.retain(message)

    val webSocket = message match {
      case request: HttpRequest => request.headers().get("Upgrade") == "websocket"
    }
    val bootstrap = new Bootstrap
    bootstrap.group(inboundChannel.eventLoop())
             .channel(classOf[NioSocketChannel])
             .handler(new ProxyOutboundInitializer(inboundChannel, webSocket))
    val future = bootstrap.connect(remoteHost, remotePort)
    outboundChannel = future.channel()
    future.addListener(new ChannelFutureListener {
      def operationComplete(future: ChannelFuture) = {
        if (future.isSuccess) {         // Connected - start reading
          outboundChannel.write(message).addListener(new ChannelFutureListener {
            def operationComplete(future: ChannelFuture) {
              context.channel.read()
            }
          })
        } else {                        // Failed to connect - close the connection
          inboundChannel.close()
          BufUtil.release(message)
        }
      }
    })
  }

  override def exceptionCaught(context: ChannelHandlerContext, cause: Throwable) = {
    cause.printStackTrace()
    context.close()
  }
}
