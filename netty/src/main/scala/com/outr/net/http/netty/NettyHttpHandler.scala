package com.outr.net.http.netty

import io.netty.channel.{Channel, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{HttpRequest => NettyHttpRequest, HttpHeaders, QueryStringDecoder}

import scala.collection.JavaConversions._
import com.outr.net.http._
import com.outr.net._
import com.outr.net.http.response.HttpResponseStatus
import com.outr.net.http.request.{HttpRequestHeaders, HttpRequest}
import java.net.InetSocketAddress
import com.outr.net.http.response.HttpResponse
import com.outr.net.http.content.URLContent

/**
 * @author Matt Hicks <matt@outr.com>
 */
class NettyHttpHandler(support: NettyHttpSupport) extends SimpleChannelInboundHandler[AnyRef] {
  def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef) = msg match {
    case nettyRequest: NettyHttpRequest => {
      val request = NettyHttpHandler.requestConverter(nettyRequest, ctx.channel())
      val response = support.application.onReceive(request, HttpResponse(status = HttpResponseStatus.NotFound))
//      val nettyResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, NettyHttpHandler.statusConverter(response.status))
//      nettyResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, response.contentType)
      response.content match {
        case c: URLContent => {
          NettyRequestHandler.streamURL(c.url.javaURL, ctx, nettyRequest, response.content.contentType.toString)
        }
      }

      println(s"Response: $response")
    }
  }
}

object NettyHttpHandler {
  private val HostAndPortRegex = """(.*?):?(\d*)""".r

  def requestConverter(r: NettyHttpRequest, channel: Channel) = {
    val url = urlExtractor(r, channel)
    val method = Method(r.getMethod.name())
    val headers = r.headers().map(entry => entry.getKey -> entry.getValue).toMap
    HttpRequest(url, method, HttpRequestHeaders(headers))
  }

  def urlExtractor(r: NettyHttpRequest, channel: Channel) = {
    val decoder = new QueryStringDecoder(r.getUri)
    val hostAndPort = HttpHeaders.getHost(r)
    val (host, port) = hostAndPort match {
      case HostAndPortRegex(h, p) => h -> (if (p != null && p != "") p.toInt else 80)
    }
    val parameters = decoder.parameters().map {
      case (key, values) => key -> values.toList
    }.toMap
    val ip = IP(channel.remoteAddress().asInstanceOf[InetSocketAddress].getAddress.getHostAddress)
    URL(Protocol.Http, host, port, ip, decoder.path(), HttpParameters(parameters))
  }

  def statusConverter(status: HttpResponseStatus) = {
    new io.netty.handler.codec.http.HttpResponseStatus(status.code, status.message)
  }
}