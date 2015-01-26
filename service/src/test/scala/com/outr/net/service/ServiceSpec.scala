package com.outr.net.service

import com.outr.net.URL
import com.outr.net.http.WebApplication
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import com.outr.net.http.session.MapSession
import org.scalatest.{Matchers, WordSpec}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ServiceSpec extends WordSpec with Matchers {
  "TestService" should {
    "configure the service" in {
      val service = Service[Receiving, Sending] {
        case receiving => {
          println(s"Receiving: $receiving")
          Sending(receiving.name.reverse)
        }
      }
      service.bindTo(TestApplication, "/service")
    }
    "call the service with GET parameters" in {
      val response = TestApplication.onReceive(HttpRequest(url = URL("http://localhost/service?name=Hello")), HttpResponse(status = HttpResponseStatus.NotFound))
      println(response.content.asString)
    }
    "unbind the service" in {

    }
    "verify the service is no longer receiving" in {

    }
  }
}

object TestApplication extends WebApplication[MapSession] {
  override protected def init() = {}

  override protected def createSession(request: HttpRequest, id: String) = new MapSession(id, this)
}

case class Receiving(name: String)

case class Sending(name: String)