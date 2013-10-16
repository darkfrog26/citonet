package com.outr.net.http.netty

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{HttpRequest => NettyHttpRequest, HttpHeaders, QueryStringDecoder}

import scala.collection.JavaConversions._
import com.outr.net.http._
import com.outr.net.{Protocol, URL, Method}
import com.outr.net.http.response.{HttpResponse, HttpResponseStatus}
import com.outr.net.http.request.{HttpRequestHeaders, HttpRequest}
import com.outr.net.http.content.URLContent

/**
 * @author Matt Hicks <matt@outr.com>
 */
class NettyHttpHandler(support: NettyHttpSupport) extends SimpleChannelInboundHandler[AnyRef] {
  def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef) = msg match {
    case nettyRequest: NettyHttpRequest => {
      val request = NettyHttpHandler.requestConverter(nettyRequest)
      val response = support.application.onReceive(request, HttpResponse(status = HttpResponseStatus.NotFound))
//      val nettyResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, NettyHttpHandler.statusConverter(response.status))
//      nettyResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, response.contentType)
      response.content match {
        case c: URLContent => {
          NettyRequestHandler.streamURL(c.url.javaURL, ctx, nettyRequest, response.content.contentType)
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
    val method = Method(r.getMethod.name())
    val headers = r.headers().map(entry => entry.getKey -> entry.getValue).toMap
    HttpRequest(url, method, HttpRequestHeaders(headers))
  }

  def urlExtractor(r: NettyHttpRequest) = {
    val decoder = new QueryStringDecoder(r.getUri)
    val hostAndPort = HttpHeaders.getHost(r)
    val (host, port) = hostAndPort match {
      case HostAndPortRegex(h, p) => h -> (if (p != null && p != "") p.toInt else 80)
    }
    val parameters = decoder.parameters().map {
      case (key, values) => key -> values.toList
    }.toMap
    URL(Protocol.Http, host, port, decoder.path(), HttpParameters(parameters))
  }

  def statusConverter(status: HttpResponseStatus) = {
    new io.netty.handler.codec.http.HttpResponseStatus(status.code, status.message)
  }
}