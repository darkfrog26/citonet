package com.outr.net.communicator.server

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class ResponseData[T](id: Int, data: T)