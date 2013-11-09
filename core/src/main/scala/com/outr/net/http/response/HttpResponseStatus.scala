package com.outr.net.http.response

import org.powerscala.enum.{EnumEntry, Enumerated}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class HttpResponseStatus private(val code: Int, val message: String) extends EnumEntry {
  def isInformation = code >= 100 && code < 200
  def isSuccess = code >= 200 && code < 300
  def isRedirection = code >= 300 && code < 400
  def isClientError = code >= 400 && code < 500
  def isServerError = code >= 500

  def isError = isClientError || isServerError
}

object HttpResponseStatus extends Enumerated[HttpResponseStatus] {
  val Continue = new HttpResponseStatus(100, "Continue")
  val SwitchingProtocols = new HttpResponseStatus(101, "Switching Protocols")
  val Processing = new HttpResponseStatus(102, "Processing")

  val OK = new HttpResponseStatus(200, "OK")
  val Created = new HttpResponseStatus(201, "Created")
  val Accepted = new HttpResponseStatus(202, "Accepted")
  val NonAuthoritativeInformation = new HttpResponseStatus(203, "Non-Authoritative Information")
  val NoContent = new HttpResponseStatus(204, "No Content")
  val ResetContent = new HttpResponseStatus(205, "Reset Content")
  val PartialContent = new HttpResponseStatus(206, "Partial Content")
  val MultiStatus = new HttpResponseStatus(207, "Multi-Status")

  val MultipleChoices = new HttpResponseStatus(300, "Multiple Choices")
  val MovedPermanently = new HttpResponseStatus(301, "Moved Permanently")
  val Found = new HttpResponseStatus(302, "Found")
  val SeeOther = new HttpResponseStatus(303, "See Other")
  val NotModified = new HttpResponseStatus(304, "Not Modified")
  val UseProxy = new HttpResponseStatus(305, "Use Proxy")
  val TemporaryRedirect = new HttpResponseStatus(307, "Temporary Redirect")

  val BadRequest = new HttpResponseStatus(400, "Bad Request")
  val Unauthorized = new HttpResponseStatus(401, "Unauthorized")
  val PaymentRequired = new HttpResponseStatus(402, "Payment Required")
  val Forbidden = new HttpResponseStatus(403, "Forbidden")
  val NotFound = new HttpResponseStatus(404, "Not Found")
  val MethodNotAllowed = new HttpResponseStatus(405, "Method Not Allowed")
  val NotAcceptable = new HttpResponseStatus(406, "Not Acceptable")
  val ProxyAuthenticationRequired = new HttpResponseStatus(407, "Proxy Authentication Required")
  val RequestTimeout = new HttpResponseStatus(408, "Request Timeout")
  val Conflict = new HttpResponseStatus(409, "Conflict")
  val Gone = new HttpResponseStatus(410, "Gone")
  val LengthRequired = new HttpResponseStatus(411, "Length Required")
  val PreconditionFailed = new HttpResponseStatus(412, "Precondition Failed")
  val RequestEntityTooLarge = new HttpResponseStatus(413, "Request Entity Too Large")
  val RequestURITooLong = new HttpResponseStatus(414, "Request-URI Too Long")
  val UnsupportedMediaType = new HttpResponseStatus(415, "Unsupported Media Type")
  val RequestedRangeNotSatisfiable = new HttpResponseStatus(416, "Requested Range Not Satisfiable")
  val ExpectationFailed = new HttpResponseStatus(417, "Expectation Failed")
  val UnprocessableEntity = new HttpResponseStatus(422, "Unprocessable Entity")
  val Locked = new HttpResponseStatus(423, "Locked")
  val FailedDependency = new HttpResponseStatus(424, "Failed Dependency")
  val UnorderedCollection = new HttpResponseStatus(425, "Unordered Collection")
  val UpgradeRequired = new HttpResponseStatus(426, "Upgrade Required")
  val PreconditionRequired = new HttpResponseStatus(428, "Precondition Required")
  val TooManyRequests = new HttpResponseStatus(429, "Too Many Requests")
  val RequestHeaderFieldsTooLarge = new HttpResponseStatus(431, "Request Header Fields Too Large")

  val InternalServerError = new HttpResponseStatus(500, "Internal Server Error")
  val NotImplemented = new HttpResponseStatus(501, "Not Implemented")
  val BadGateway = new HttpResponseStatus(502, "Bad Gateway")
  val ServiceUnavailable = new HttpResponseStatus(503, "Service Unavailable")
  val GatewayTimeout = new HttpResponseStatus(504, "Gateway Timeout")
  val HTTPVersionNotSupported = new HttpResponseStatus(505, "HTTP Version Not Supported")
  val VariantAlsoNegotiates = new HttpResponseStatus(506, "Variant Also Negotiates")
  val InsufficientStorage = new HttpResponseStatus(507, "Insufficient Storage")
  val NotExtended = new HttpResponseStatus(510, "Not Extended")
  val NetworkAuthenticationRequired = new HttpResponseStatus(511, "Network Authentication Required")
}
