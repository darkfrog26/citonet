package com.outr.net.communicator.server

/**
 * @author Matt Hicks <matt@outr.com>
 */
class MessageException(message: String, val failure: MessageReceiveFailure) extends RuntimeException(message)