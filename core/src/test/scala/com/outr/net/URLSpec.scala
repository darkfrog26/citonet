package com.outr.net

import org.scalatest.{Matchers, WordSpec}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class URLSpec extends WordSpec with Matchers {
  "URL" when {
    "parsing" should {
      "properly parse a simple URL" in {
        val url = URL("http://www.outr.com")
        url.domain should equal("outr.com")
        url.host should equal("www.outr.com")
        url.protocol should equal(Protocol.Http)
        url.path should equal("/")
        url.port should equal(80)
      }
      "properly parse a relative URL" in {
        val url = URL("http://www.outr.com/examples/../images/test.png")
        url.path should equal("/images/test.png")
      }
      "properly parse a relative URL with invalid higher level" in {
        val url = URL("http://www.outr.com/../images/test.png")
        url.path should equal("/images/test.png")
      }
      "properly parse a relative URL with multiple higher levels" in {
        val url = URL("http://www.outr.com/examples/testing/../../images/../test.png")
        url.path should equal("/test.png")
      }
    }
  }
}
