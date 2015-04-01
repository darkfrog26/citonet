package com.outr.net.http.client

import com.outr.net.URL
import com.outr.net.http.content.ContentType
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.HttpResponseStatus
import org.scalatest.{Matchers, WordSpec}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class HttpClientSpec extends WordSpec with Matchers {
  val client = ApacheHttpClient

  "HttpClient" should {
    "do a request to captiveimagination.com and get proper headers and content back" in {
      val request = HttpRequest(URL.encoded("http://www.captiveimagination.com"))
      val response = client.send(request)
      response.status should equal(HttpResponseStatus.OK)
      response.content.asString.trim should equal("captiveimagination.com")
      response.headers("Server") should equal("Apache/2.2.14 (Ubuntu)")
      response.headers("Accept-Ranges") should equal("bytes")
      response.headers("Connection") should equal("Keep-Alive")
      response.headers("Content-Type") should equal("text/html")
      response.cookies.size should equal(0)
    }
    "do a request to google.com and get proper content response" in {
      val request = HttpRequest(URL.encoded("http://www.google.com"))
      val response = client.send(request)
      response.status should equal(HttpResponseStatus.OK)
      response.headers("Server") should equal("gws")
      response.headers("Content-Type") should equal("text/html; charset=ISO-8859-1")
      val cookie = response.cookies.find(c => c.name == "PREF").get
      cookie.value should include("ID=")
      cookie.value should include(":FF=0:TM=")
      cookie.domain should equal("google.com")
      cookie.httpOnly should equal(false)
      cookie.secure should equal(false)
      cookie.version should equal(0)
      response.content.contentType should equal(ContentType.HTML.copy(charSet = "ISO-8859-1"))
      response.content.asString should include("<title>Google</title>")
    }
  }
}
