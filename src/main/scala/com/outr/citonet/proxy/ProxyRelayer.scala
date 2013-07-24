package com.outr.citonet.proxy

import io.netty.handler.codec.http.websocketx.{WebSocketVersion, WebSocketClientHandshakerFactory, WebSocketServerHandshakerFactory}
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpClientCodec, FullHttpRequest}
import io.netty.channel._
import java.net.URI

import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.bootstrap.Bootstrap
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.socket.SocketChannel
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ProxyRelayer extends Logging {
  def relayWebSocket(remoteHost: String, remotePort: Int, ctx: ChannelHandlerContext, req: FullHttpRequest) = {
    val localUrl = s"ws://${req.headers().get(HOST)}${req.getUri}"
    val subprotocols: String = null
    val allowExtensions = false
    val factory = new WebSocketServerHandshakerFactory(localUrl, subprotocols, allowExtensions)
    val handshaker = factory.newHandshaker(req)
    if (handshaker == null) {
      WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel())
    } else {
      handshaker.handshake(ctx.channel(), req).addListener(new ChannelFutureListener {
        def operationComplete(future: ChannelFuture) = {
          if (future.isSuccess) {
            val remoteUrl = s"ws://$remoteHost${req.getUri}"
            val uri = new URI(remoteUrl)
            val handler = new WebSocketClientHandler(WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, null))
            val bootstrap = new Bootstrap
            bootstrap.channel(classOf[NioSocketChannel])
                     .group(Shared.workerGroup)
                     .handler(new ChannelInitializer[SocketChannel] {
              def initChannel(ch: SocketChannel) = {
                val pipeline = ch.pipeline()
                pipeline.addLast("codec-http", new HttpClientCodec)
                pipeline.addLast("aggregator", new HttpObjectAggregator(655360))
                pipeline.addLast("handler", handler)
              }
            })
            bootstrap.option(ChannelOption.TCP_NODELAY, new java.lang.Boolean(true))

            val ch = bootstrap.connect(remoteHost, remotePort).sync().channel()   // TODO: use future
            handler.handshakeFuture.sync()

            Shared.webSocketToRemote.put(ctx.channel(), ch)
            Shared.webSocketToRemote.put(ch, ctx.channel())
          } else {
            info("WebSocket client handshake failure!")
          }
        }
      })
    }
  }

  def relayHttpRequest(remoteHost: String, remotePort: Int, channel: Channel, req: FullHttpRequest) = {
    val handler = new HttpClientHandler(channel)
    val bootstrap = new Bootstrap
    bootstrap.channel(classOf[NioSocketChannel])
             .group(Shared.workerGroup)
             .handler(new ChannelInitializer[SocketChannel] {
      def initChannel(ch: SocketChannel) = {
        val pipeline = ch.pipeline()
        pipeline.addLast("codec-http", new HttpClientCodec)
        pipeline.addLast("aggregator", new HttpObjectAggregator(655360))
        pipeline.addLast("handler", handler)
      }
    })
    bootstrap.option(ChannelOption.TCP_NODELAY, new java.lang.Boolean(true))
    bootstrap.connect(remoteHost, remotePort).addListener(new ChannelFutureListener {
      def operationComplete(future: ChannelFuture) = {
        if (future.isSuccess) {
          future.channel().writeAndFlush(req)
        } else {
          warn("Proxy connect failed!")
        }
      }
    })
  }
}
