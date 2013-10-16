package com.outr.net.http.session

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class SessionValueChange(key: Any, oldValue: Option[Any], newValue: Option[Any])