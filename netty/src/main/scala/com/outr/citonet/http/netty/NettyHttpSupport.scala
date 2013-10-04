package com.outr.citonet.http.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.Channel
import com.outr.citonet.http.HttpApplication
import com.outr.citonet.HasHostAndPort

/**
 * @author Matt Hicks <matt@outr.com>
 */
class NettyHttpSupport(val application: HttpApplication) {
  val bootstrap = new ServerBootstrap()
  val parentGroup = new NioEventLoopGroup()
  val childGroup = new NioEventLoopGroup()

  private var channels = Map.empty[HasHostAndPort, Channel]

  init()

  private def init() = {
    bootstrap.group(parentGroup, childGroup)
    bootstrap.channel(classOf[NioServerSocketChannel])
    bootstrap.childHandler(new NettyHttpInitializer(this))

    application.bindings().foreach(bind)    // Bind to existing hosts and ports
    application.bindings.change.on {
      case evt => {
        evt.newValue.foreach {              // Add any new bindings
          case hp => if (!evt.oldValue.contains(hp)) bind(hp)
        }
        evt.oldValue.foreach {              // Remove any old bindings
          case hp => if (!evt.newValue.contains(hp)) unbind(hp)
        }
      }
    }
  }

  def bind(hostAndPort: HasHostAndPort) = synchronized {
    val host = hostAndPort.host
    val port = hostAndPort.port
    val channel = if (host != null && host != "") {
      bootstrap.bind(host, port).sync().channel()
    } else {
      bootstrap.bind(port).sync().channel()
    }
    channels += hostAndPort -> channel
  }

  def unbind(hostAndPort: HasHostAndPort) = synchronized {
    channels.get(hostAndPort) match {
      case Some(channel) => {
        channel.close()
        channels -= hostAndPort
        true
      }
      case None => false
    }
  }

  def shutdown() = channels.keys.foreach(unbind)
}