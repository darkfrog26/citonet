package com.outr.net.http.session

import org.powerscala.MappedStorage

/**
 * @author Matt Hicks <matt@outr.com>
 */
class MapSession(val id: String, val application: SessionApplication) extends Session with MappedStorage[Any, Any]