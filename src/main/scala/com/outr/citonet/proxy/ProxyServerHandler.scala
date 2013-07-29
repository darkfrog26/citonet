package com.outr.citonet.proxy

import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.websocketx._
import io.netty.handler.codec.http.{DefaultFullHttpResponse, FullHttpResponse, FullHttpRequest}
import io.netty.buffer.Unpooled
import io.netty.util.CharsetUtil

import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http.HttpHeaders._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpMethod._
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProxyServerHandler extends SimpleChannelInboundHandler[AnyRef] with Logging {
  def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef) = msg match {
    case req: FullHttpRequest => handleHttpRequest(ctx, req)
    case frame: WebSocketFrame => handleWebSocketFrame(ctx, frame)
  }

  override def channelReadComplete(ctx: ChannelHandlerContext) = {
    ctx.flush()
  }

  override def channelInactive(ctx: ChannelHandlerContext) {
    // TODO: disconnect all clients
  }

  var handshaker: WebSocketServerHandshaker = _
  val remoteHost = "projectspeaker.com"
  val remotePort = 80

  private def handleHttpRequest(ctx: ChannelHandlerContext, req: FullHttpRequest) = {
    if (!req.getDecoderResult.isSuccess) {
      sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST))
    } else if ("websocket".equals(req.headers().get("Upgrade")) && req.getMethod == GET) {
      ProxyRelayer.relayWebSocket(this, remoteHost, remotePort, ctx, req)
    } else {
      ProxyRelayer.relayHttpRequest(remoteHost, remotePort, ctx.channel(), req)
    }
  }

  private def handleWebSocketFrame(ctx: ChannelHandlerContext, frame: WebSocketFrame) = frame match {
    case f: CloseWebSocketFrame => handshaker.close(ctx.channel(), f)
    case f: PingWebSocketFrame => ctx.channel().write(new PongWebSocketFrame(frame.content().retain(1)))
    case f: TextWebSocketFrame => {
      val remote = Shared.webSocketToRemote.get(ctx.channel())
      remote.writeAndFlush(frame.retain(1))
    }
    case _ => throw new UnsupportedOperationException(s"${frame.getClass} frame types are not supported")
  }

  private def sendHttpResponse(ctx: ChannelHandlerContext, req: FullHttpRequest, res: FullHttpResponse) = {
    if (res.getStatus.code() != 200) {
      val buf = Unpooled.copiedBuffer(res.getStatus.toString, CharsetUtil.UTF_8)
      res.content().writeBytes(buf)
      setContentLength(res, res.content().readableBytes())
    }

    // TODO: validate isKeepAlive(req)?
    ctx.channel().writeAndFlush(res).addListener(ChannelFutureListener.CLOSE)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    error("An exception occurred in ProxyServerHandler", cause)
    ctx.close()
  }
}