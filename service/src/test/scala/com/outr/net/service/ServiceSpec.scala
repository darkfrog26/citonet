package com.outr.net.service

import com.outr.net.{Method, URL}
import com.outr.net.http.WebApplication
import com.outr.net.http.content.{StringContent, ContentType}
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import com.outr.net.http.session.MapSession
import org.json4s.JsonAST.JString
import org.powerscala.json.MapSupport
import org.scalatest.{Matchers, WordSpec}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ServiceSpec extends WordSpec with Matchers {
  "TestService" should {
    "configure the service" in {
      ReverseService.bindTo(TestApplication, "/service")
    }
    "call the service with GET parameters" in {
      val response = TestApplication.onReceive(HttpRequest(url = URL("http://localhost/service?name=Hello")), HttpResponse(status = HttpResponseStatus.NotFound))
      response.status should equal(HttpResponseStatus.OK)
      response.content.contentType should equal(ContentType.JSON)
      response.content.asString should equal("""{"name":"olleH"}""")
    }
    "call the service with POST JSON" in {
      val content = StringContent("""{"name":"Hello"}""", ContentType.JSON)
      val response = TestApplication.onReceive(HttpRequest(url = URL("http://localhost/service"), method = Method.Post, content = Some(content)), HttpResponse(status = HttpResponseStatus.NotFound))
      response.status should equal(HttpResponseStatus.OK)
      response.content.contentType should equal(ContentType.JSON)
      response.content.asString should equal("""{"name":"olleH"}""")
    }
    "unbind the service" in {
      ReverseService.unbindFrom(TestApplication, "/service")
    }
    "verify the service is no longer receiving" in {
      TestApplication.onReceive(HttpRequest(url = URL("http://localhost/service?name=Hello")), HttpResponse(status = HttpResponseStatus.NotFound)).status should equal(HttpResponseStatus.NotFound)
    }
  }
}

object TestApplication extends WebApplication[MapSession] {
  override protected def init() = {}

  override protected def createSession(request: HttpRequest, id: String) = new MapSession(id, this)
}

object ReverseService extends Service[Receiving, Sending] {
  MapSupport.o2j.removeWhen("class", JString("com.outr.net.service.Sending"))

  override def apply(receiving: Receiving) = Sending(receiving.name.reverse)
}

case class Receiving(name: String)

case class Sending(name: String)