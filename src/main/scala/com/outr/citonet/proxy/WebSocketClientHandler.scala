package com.outr.citonet.proxy

import io.netty.channel.{ChannelPromise, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx._
import io.netty.handler.codec.http.FullHttpResponse

/**
 * @author Matt Hicks <matt@outr.com>
 */
class WebSocketClientHandler(handshaker: WebSocketClientHandshaker) extends SimpleChannelInboundHandler[AnyRef] {
  var handshakeFuture: ChannelPromise = _

  def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef) = {
    val channel = ctx.channel()
    msg match {
      case res: FullHttpResponse if !handshaker.isHandshakeComplete => {
        handshaker.finishHandshake(channel, res)
      }
      case frame: WebSocketFrame => {
        val webSocket = Shared.remoteToWebSocket.get(ctx.channel())
        webSocket.writeAndFlush(frame.retain(1))
        frame match {
          case f: CloseWebSocketFrame => channel.close()
          case _ => // Ignore others
        }
      }
    }
  }

  override def handlerAdded(ctx: ChannelHandlerContext) {
    handshakeFuture = ctx.newPromise()
  }

  override def channelActive(ctx: ChannelHandlerContext) {
    handshaker.handshake(ctx.channel())
  }

  override def channelInactive(ctx: ChannelHandlerContext) {
    println("WebSocket client disconnected")
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace()

    if (!handshakeFuture.isDone) {
      handshakeFuture.setFailure(cause)
    }

    ctx.close()
  }
}
