package com.outr.net.http.response

import com.outr.net.http.HttpHeaders

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class HttpResponseHeaders(values: Map[String, String] = Map.empty) extends HttpHeaders {
  def CacheControl(value: String = "no-cache, max-age=0, must-revalidate, no-store") = {
    copy(values + ("Cache-Control" -> value))
  }
  def Location(url: String) = {
    copy(values + ("Location" -> url))
  }

  /**
   * The disposition of the content being sent back in the response.
   *
   * @param dispositionType generally "inline" and "attachment" are the only options used
   * @param filename the filename representation of the content sent back to the client
   */
  def ContentDisposition(dispositionType: String, filename: String) = {
    copy(values + ("Content-Disposition" -> "%s; filename=\"%s\"".format(dispositionType, filename)))
  }

  def merge(headers: HttpHeaders) = {
    HttpResponseHeaders(values ++ headers.values)
  }
}