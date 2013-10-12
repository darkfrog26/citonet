package com.outr.net.communicator.server

import com.outr.net.http.HttpHandler
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponse
import com.outr.net.http.content.{InputStreamContent, StringContent}

import org.powerscala.json._
import org.powerscala.IO
import scala.util.parsing.json.JSON

/**
 * @author Matt Hicks <matt@outr.com>
 */
object CommunicatorHandler extends HttpHandler {
  def onReceive(request: HttpRequest) = {
    val data = request.content match {
      case Some(content) => content match {
        case isc: InputStreamContent => JSON.parseFull(IO.copy(isc.input))
      }
      case None => null
    }
    println(s"Communication received: $request - Data: $data")
    val response = List(ResponseData(1, "Hello World"), ResponseData(2, "Goodbye World"))
    val json = generate(response, specifyClassName = false)
    HttpResponse(StringContent(json, "application/json"))
  }
}