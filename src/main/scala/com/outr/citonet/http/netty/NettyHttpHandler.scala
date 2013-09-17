package com.outr.citonet.http.netty

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{HttpRequest => NettyHttpRequest, HttpHeaders, QueryStringDecoder}

import scala.collection.JavaConversions._
import com.outr.citonet.{Protocol, Method, URL}
import com.outr.citonet.http.{HttpParameters, HttpRequest}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class NettyHttpHandler(support: NettyHttpSupport) extends SimpleChannelInboundHandler[AnyRef] {
  def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef) = msg match {
    case request: NettyHttpRequest => {
      val httpRequest = NettyHttpHandler.requestConverter(request)
      val response = support.application.onReceive(httpRequest)
      println(s"Response: $response")
    }
  }
}

object NettyHttpHandler {
  private val HostAndPortRegex = """(.*?):?(\d*)""".r

  def requestConverter(r: NettyHttpRequest) = {
    val url = urlExtractor(r)
    val headers = r.headers().map(entry => entry.getKey -> entry.getValue).toMap
    HttpRequest(url, headers)
  }

  def urlExtractor(r: NettyHttpRequest) = {
    val method = Method(r.getMethod.name())
    val decoder = new QueryStringDecoder(r.getUri)
    val hostAndPort = HttpHeaders.getHost(r)
    val (host, port) = hostAndPort match {
      case HostAndPortRegex(h, p) => h -> (if (p != null && p != "") p.toInt else 80)
    }
    val parameters = decoder.parameters().map {
      case (key, values) => key -> values.toList
    }.toMap
    URL(method, Protocol.Http, host, port, decoder.path(), HttpParameters(parameters))
  }
}