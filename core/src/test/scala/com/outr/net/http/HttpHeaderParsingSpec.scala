package com.outr.net.http

import java.util.Locale

import com.outr.net.http.request.HttpRequestHeaders
import org.scalatest.{Matchers, WordSpec}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class HttpHeaderParsingSpec extends WordSpec with Matchers {
  "HttpHeaders" when {
    "parsing" should {
      "properly parse a date in another locale" in {
        val acceptLanguage = "pl,en;q=0.5"
        val ifModifiedSince = "Wt, 23 cze 2015 21:35:01 GMT"

        val headers = HttpRequestHeaders(Map(HttpRequestHeaders.IfModifiedSince -> ifModifiedSince, HttpRequestHeaders.AcceptLanguage -> acceptLanguage))
        headers.locale should equal(Some(Locale.forLanguageTag("pl")))
        headers.IfModifiedSince should equal(Some(1435095301000L))
      }
    }
  }
}