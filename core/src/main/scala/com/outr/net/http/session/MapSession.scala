package com.outr.net.http.session

import org.powerscala.MappedStorage

/**
 * @author Matt Hicks <matt@outr.com>
 */
class MapSession extends MappedStorage[Any, Any] with Session