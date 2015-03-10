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
      "properly parse an extremely long URL and spit it back syntactically equal" in {
        val s = "http://www.a.com.qa/pps/a/publish/Pages/System+Pages/Document+View+Page?com.a.b.pagesvc.renderParams.sub-53343f7a_1279673d2a9_-78af0a000136=rp.currentDocumentID%3D-4591476d_14a4cb0cbbf_-6cb00a000121%26"
        val url = URL(s)
        url.protocol should equal(Protocol.Http)
        url.host should equal("www.a.com.qa")
        url.path should equal("/pps/a/publish/Pages/System+Pages/Document+View+Page")
        url.parameters.first("com.a.b.pagesvc.renderParams.sub-53343f7a_1279673d2a9_-78af0a000136") should equal("rp.currentDocumentID%3D-4591476d_14a4cb0cbbf_-6cb00a000121%26")
        url.toString should equal(s)
        val decoded = url.decoded
        decoded.path should equal("/pps/a/publish/Pages/System Pages/Document View Page")
        decoded.parameters.first("com.a.b.pagesvc.renderParams.sub-53343f7a_1279673d2a9_-78af0a000136") should equal("rp.currentDocumentID=-4591476d_14a4cb0cbbf_-6cb00a000121&")
        val encoded = decoded.encoded
        encoded.path should equal("/pps/a/publish/Pages/System+Pages/Document+View+Page")
        encoded.parameters.first("com.a.b.pagesvc.renderParams.sub-53343f7a_1279673d2a9_-78af0a000136") should equal("rp.currentDocumentID%3D-4591476d_14a4cb0cbbf_-6cb00a000121%26")
      }
    }
  }
}
