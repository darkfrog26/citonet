package com.outr.citonet.http.netty

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{HttpRequest => NettyHttpRequest, HttpHeaders, QueryStringDecoder}

import scala.collection.JavaConversions._
import com.outr.citonet.http._
import com.outr.citonet.{Protocol, URL, Method}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class NettyHttpHandler(support: NettyHttpSupport) extends SimpleChannelInboundHandler[AnyRef] {
  def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef) = msg match {
    case nettyRequest: NettyHttpRequest => {
      val request = NettyHttpHandler.requestConverter(nettyRequest)
      val response = support.application.onReceive(request)
//      val nettyResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, NettyHttpHandler.statusConverter(response.status))
//      nettyResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, response.contentType)
      response.content match {
        case c: URLResponseContent => {
          NettyRequestHandler.streamURL(c.url.javaURL, ctx, nettyRequest, response.contentType)
        }
      }

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

  def statusConverter(status: HttpResponseStatus) = {
    new io.netty.handler.codec.http.HttpResponseStatus(status.code, status.message)
  }
}