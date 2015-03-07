package com.outr.net.service

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ServiceException(val message: String, val code: Int, val cause: Throwable = null) extends RuntimeException(message, cause) {
  lazy val response = ExceptionResponse(message, code)
}

case class ExceptionResponse(message: String, code: Int)