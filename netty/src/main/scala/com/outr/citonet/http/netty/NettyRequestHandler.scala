package com.outr.citonet.http.netty

import java.util.{Date, TimeZone, Locale, Calendar}
import java.text.SimpleDateFormat
import java.io.{RandomAccessFile, File, InputStream}
import java.net.URL
import javax.activation.MimetypesFileTypeMap
import org.powerscala.IO
import io.netty.channel._
import io.netty.handler.codec.http._
import io.netty.handler.stream.{ChunkedFile, ChunkedStream}
import io.netty.handler.ssl.SslHandler

object NettyRequestHandler {
  /**
   * The default chunk size to use for streaming methods in RequestHandler.
   *
   * Defaults to 8192
   */
  val chunkSize = 8192
  /**
   * The default cache time to use for streaming methods in RequestHandler.
   *
   * Defaults to 120
   */
  val cacheTimeInSeconds = 120
  /**
   * The default mimeTypes to use for streaming methods in RequestHandler.
   */
  val mimeTypes = new MimetypesFileTypeMap() {
    addMimeTypes(IO.copy(getClass.getClassLoader.getResource("mime_types.txt")))
  }

  def createDateFormatter: SimpleDateFormat = {     // DateFormatter isn't thread-safe
  val f = new SimpleDateFormat("EEE, dd MMM yyy HH:mm:ss zzz", Locale.US)
    f.setTimeZone(TimeZone.getTimeZone("GMT"))
    f
  }

  /**
   * Streams a URL back to the client and closes the connection when finished.
   *
   * @param url the URL to stream
   * @param context the context to use
   * @param request the inbound request
   * @param contentType the content-type to send. If this is null the content-type will be derived from the URL
   * @param cacheTimeInSeconds the amount of time in seconds to cache this response
   * @param chunkSize the chunk size to use when streaming the content back
   * @param enableCaching determines whether this content is cachable
   */
  def streamURL(url: URL,
                context: ChannelHandlerContext,
                request: HttpRequest,
                contentType: String = null,
                cacheTimeInSeconds: Int = cacheTimeInSeconds,
                chunkSize: Int = chunkSize,
                enableCaching: Boolean = true) = {
    val connection = url.openConnection()
    val lastModified = connection.getLastModified
    if (!enableCaching || !sendCached(lastModified, context, request)) {
      val contentLength = connection.getContentLength
      val input = connection.getInputStream
      try {
        val path = url.toString
        val filename = if (path.indexOf('/') != -1) {
          path.substring(path.lastIndexOf('/'))
        } else {
          path
        }
        val response = createResponse(contentLength = contentLength,
          contentType = getContentType(filename, contentType),
          cacheTimeInSeconds = cacheTimeInSeconds,
          lastModified = lastModified,
          sendExpiration = enableCaching)
        val channel = context.channel()
        channel.write(response)
        writeInput(channel, input, chunkSize, closeOnFinish = true)
      } catch {
        case t: Throwable => {
          try {
            input.close()
          } catch {
            case exc: Throwable => // Ignore
          }
          throw t
        }
      }
    }
  }

  /**
   * Streams an InputStream back to the client and closes the connection when finished.
   *
   * @param input the InputStream to stream
   * @param context the context to use
   * @param request the inbound request
   * @param contentType the content-type to send
   * @param contentLength the content-length to send
   * @param lastModified the last modified time in milliseconds
   * @param cacheTimeInSeconds the amount of time in seconds to cache this response
   * @param chunkSize the chunk size to use when streaming the content back
   * @param enableCaching determines whether this content is cachable
   */
  def streamInput(input: InputStream,
                  context: ChannelHandlerContext,
                  request: HttpRequest,
                  contentType: String,
                  contentLength: Long = -1,
                  lastModified: Long = -1,
                  cacheTimeInSeconds: Int = cacheTimeInSeconds,
                  chunkSize: Int = chunkSize,
                  enableCaching: Boolean = true) = {
    if (!enableCaching || !sendCached(lastModified, context, request)) {
      val response = createResponse(contentLength = contentLength,
        contentType = contentType,
        cacheTimeInSeconds = cacheTimeInSeconds,
        lastModified = lastModified,
        sendExpiration = enableCaching)
      val channel = context.channel()
      channel.write(response)
      writeInput(channel, input, chunkSize, closeOnFinish = true)
    }
  }

  /**
   * Streams a File back to the client and closes the connection when finished.
   *
   * @param file the File to stream
   * @param context the context to use
   * @param request the inbound request
   * @param contentType the content-type to send. If this is null the content-type will be derived from the URL
   * @param cacheTimeInSeconds the amount of time in seconds to cache this response
   * @param chunkSize the chunk size to use when streaming the content back
   * @param enableCaching determines whether this content is cachable
   */
  def streamFile(file: File,
                 context: ChannelHandlerContext,
                 request: HttpRequest,
                 contentType: String = null,
                 cacheTimeInSeconds: Int = cacheTimeInSeconds,
                 chunkSize: Int = chunkSize,
                 enableCaching: Boolean = true) = {
    if (!enableCaching || !sendCached(file.lastModified(), context, request)) {
      val raf = new RandomAccessFile(file, "r")
      val response = createResponse(contentLength = raf.length(),
        contentType = getContentType(file.getName, contentType),
        cacheTimeInSeconds = cacheTimeInSeconds,
        lastModified = file.lastModified(),
        sendExpiration = enableCaching)
      val channel = context.channel()
      channel.write(response)
      writeFile(channel, raf, chunkSize, closeOnFinish = true)
    }
  }

  /*
   * Streams a String back to the client and closes the connection when finished.
   *
   * @param s the String to stream
   * @param context the context to use
   * @param request the inbound request
   * @param contentType the content-type to send
   * @param lastModified the last modified time in milliseconds
   * @param cacheTimeInSeconds the amount of time in seconds to cache this response
   * @param enableCaching determines whether this content is cachable
   * @param status defines the HttpResponseStatus (default to OK)
   */
  /*def streamString(s: String,
                   context: ChannelHandlerContext,
                   request: HttpRequest,
                   contentType: String,
                   lastModified: Long = -1,
                   cacheTimeInSeconds: Int = cacheTimeInSeconds,
                   enableCaching: Boolean = true,
                   status: HttpResponseStatus = HttpResponseStatus.OK) = {
    if (!enableCaching || !sendCached(lastModified, context, request)) {
      val content = ChannelBuffers.copiedBuffer(s, CharsetUtil.UTF_8)
      val response = createResponse(status = status,
        contentLength = content.readableBytes(),
        contentType = "%s; charset=UTF-8".format(contentType),
        cacheTimeInSeconds = cacheTimeInSeconds,
        lastModified = lastModified,
        sendExpiration = enableCaching)
      response.setContent(content)
      val channel = context.getChannel
      channel.write(response).addListener(ChannelFutureListener.CLOSE)
    }
  }*/

  /**
   * Sends a standard OK response and closes the connection.
   */
  def sendResponse(context: ChannelHandlerContext) = {
    val response = createResponse()
    context.channel().write(response).addListener(ChannelFutureListener.CLOSE)
  }

  /**
   * Attempts to use mimeTypes to determine the content-type if the supplied content-type is null.
   *
   * @param filename the filename to use for content-type derivation
   * @param contentType the content-type to use if not null
   * @return derived content-type
   */
  def getContentType(filename: String, contentType: String = null) = contentType match {
    case null => mimeTypes.getContentType(filename)
    case s => s
  }

  /**
   * Reads from the request the last modified date in the headers and determines if the version the client has the same
   * modification stamp as the server.
   *
   * Note that this will send a response back and close the connection if the client has the cached version.
   *
   * @param lastModified the last modified stamp in milliseconds to compare with
   * @param context the context to use
   * @param request the inbound request
   * @return true if the client has the correct version
   */
  def sendCached(lastModified: Long, context: ChannelHandlerContext, request: HttpRequest) = {
    val ifModifiedSince = request.headers().get(HttpHeaders.Names.IF_MODIFIED_SINCE)
    if (ifModifiedSince != null && ifModifiedSince != "" && lastModified != -1L) {
      val date = createDateFormatter.parse(ifModifiedSince)
      val seconds = date.getTime / 1000
      val lastModifiedSeconds = lastModified / 1000
      if (seconds == lastModifiedSeconds) {
        val response = createResponse(status = HttpResponseStatus.NOT_MODIFIED, sendExpiration = false)
        context.channel().write(response).addListener(ChannelFutureListener.CLOSE)
        true
      } else {
        false
      }
    } else {
      false
    }
  }

  /**
   * Creates an HttpResponse given the parameters supplied.
   *
   * @param version the HttpVersion, defaults to HTTP_1_1
   * @param status the HttpResponseStatus, defaults to OK
   * @param contentLength the content-length, defaults to -1 (not sent)
   * @param contentType the content-type, defaults to null (not sent)
   * @param cacheTimeInSeconds the time to cache the response in seconds
   * @param lastModified the last modified timestamp, defaults to -1 (not sent)
   * @param sendExpiration true if expiration data should be sent, defaults to true
   */
  def createResponse(version: HttpVersion = HttpVersion.HTTP_1_1,
                     status: HttpResponseStatus = HttpResponseStatus.OK,
                     contentLength: Long = -1,
                     contentType: String = null,
                     cacheTimeInSeconds: Int = cacheTimeInSeconds,
                     lastModified: Long = -1,
                     sendExpiration: Boolean = true) = {
    val r = new DefaultHttpResponse(version, status)
    if (contentLength != -1) {
      HttpHeaders.setContentLength(r, contentLength)
    }
    if (contentType != null) {
      r.headers().set(HttpHeaders.Names.CONTENT_TYPE, contentType)
    }
    val c = Calendar.getInstance()
    val f = createDateFormatter
    r.headers().set(HttpHeaders.Names.DATE, f.format(c.getTime))
    if (sendExpiration) {
      c.add(Calendar.SECOND, cacheTimeInSeconds)
      r.headers().set(HttpHeaders.Names.EXPIRES, f.format(c.getTime))
      r.headers().set(HttpHeaders.Names.CACHE_CONTROL, "private, max-age=%s".format(cacheTimeInSeconds))
    }
    if (lastModified != -1L) {
      r.headers().set(HttpHeaders.Names.LAST_MODIFIED, f.format(new Date(lastModified)))
    }
    r
  }

  /**
   * Writes the InputStream out. This handles explicitly streaming data back to the channel. The streamInput method
   * should be used to send a response instead of this method.
   */
  def writeInput(channel: Channel, input: InputStream, chunkSize: Int = chunkSize, closeOnFinish: Boolean = true) = {
    if (input == null) {
      throw new NullPointerException("Input cannot be null")
    }
    try {
      val f = channel.write(new ChunkedStream(input, chunkSize))
      f.addListener(new ChannelFutureListener {
        def operationComplete(future: ChannelFuture) = input.close()
      })
      if (closeOnFinish) {
        f.addListener(ChannelFutureListener.CLOSE)
      }
      f
    } catch {
      case t: Throwable => {
        try {
          input.close()
        } catch {
          case exc: Throwable => // Ignore errors closing
        }
        throw t
      }
    }
  }

  /**
   * Writes the RandomAccessFile out. This handles explicitly streaming data back to the channel. The streamFile method
   * should be used to send a response instead of this method.
   */
  def writeFile(channel: Channel, file: RandomAccessFile, chunkSize: Int = chunkSize, closeOnFinish: Boolean = true) = {
    val f = if (channel.pipeline().get(classOf[SslHandler]) != null) {
      channel.write(new ChunkedFile(file, 0, file.length(), chunkSize))
    } else {
      val region = new DefaultFileRegion(file.getChannel, 0, file.length())
      val future = channel.write(region)
//      future.addListener(new ChannelFutureListener {
//        def operationComplete(future: ChannelFuture) {
//          region.releaseExternalResources()
//        }
//      })
      future
    }
    if (closeOnFinish) {
      f.addListener(new ChannelFutureListener {
        def operationComplete(future: ChannelFuture) = file.close()
      })
      f.addListener(ChannelFutureListener.CLOSE)
    }
    f
  }

//  def respond(context: ChannelHandlerContext, status: HttpResponseStatus) = {
//    val response = createResponse(status = status, contentType = "text/plain; charset=UTF-8")
//    response.setContent(ChannelBuffers.copiedBuffer(status.toString, CharsetUtil.UTF_8))
//    context.getChannel.write(response).addListener(ChannelFutureListener.CLOSE)
//  }
//
//  def responder(status: HttpResponseStatus): RequestHandler = new RequestHandler {
//    def apply(webapp: NettyWebapp, context: ChannelHandlerContext, event: MessageEvent) = {
//      respond(context, status)
//    }
//  }

//  def redirect(url: String, context: ChannelHandlerContext) = {
//    val s = "<html><head><meta http-equiv=\"refresh\" content=\"0;url=%s\"></head><body>redirect</body></html>".format(url)
//    val content = ChannelBuffers.copiedBuffer(s, CharsetUtil.UTF_8)
//    val response = createResponse(status = HttpResponseStatus.FOUND,
//      contentLength = content.readableBytes(),
//      contentType = "text/html; charset=UTF-8",
//      cacheTimeInSeconds = cacheTimeInSeconds,
//      sendExpiration = false)
//    response.setHeader(HttpHeaders.Names.LOCATION, url)
//    response.setContent(content)
//    val channel = context.getChannel
//    channel.write(response).addListener(ChannelFutureListener.CLOSE)
//  }

//  def redirector(url: String) = new RequestHandler {
//    def apply(webapp: NettyWebapp, context: ChannelHandlerContext, event: MessageEvent) = {
//      redirect(url, context)
//    }
//  }
}