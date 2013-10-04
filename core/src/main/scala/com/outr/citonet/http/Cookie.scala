package com.outr.citonet.http

/**
 * Cookie represents a request or response cookie. For requests only name and value are populated.
 *
 * To remove a cookie set maxAge to 0 or -1 for a session cookie.
 *
 * @author Matt Hicks <matt@outr.com>
 */
case class Cookie(name: String,
                  value: String,
                  comment: String = null,
                  commentUrl: String = null,
                  domain: String = null,
                  httpOnly: Boolean = false,
                  maxAge: Int = Int.MinValue,
                  path: String = "/",
                  ports: Set[Int] = Set.empty,
                  secure: Boolean = false,
                  version: Int = 0)