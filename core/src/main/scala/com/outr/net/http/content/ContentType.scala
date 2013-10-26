package com.outr.net.http.content

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class ContentType(mimeType: String, charSet: String = null, boundary: String = null) {
  lazy val outputString = {
    val b = new StringBuilder(mimeType)
    if (charSet != null) {
      b.append(s"; charset=$charSet")
    }
    if (boundary != null) {
      b.append(s"; boundary=$boundary")
    }
    b.toString()
  }

  def is(contentType: ContentType) = contentType.mimeType == mimeType

  override def toString = outputString
}

object ContentType {
  lazy val Plain = new ContentType("text/plain")
  lazy val HTML = new ContentType("text/html")
  lazy val JSON = new ContentType("application/json")
  lazy val MultiPartFormData = new ContentType("multipart/form-data")

  def parse(contentTypeString: String) = if (contentTypeString != null) {
    val parts = contentTypeString.split(';')
    var contentType = ContentType(parts(0).trim)
    parts.tail.foreach {
      case s => {
        val block = s.trim
        val divider = block.indexOf('=')
        if (divider == -1) {
          throw new RuntimeException(s"Unable to parse content type: [$contentTypeString]")
        }
        val name = block.substring(0, divider)
        val value = block.substring(divider + 1)
        name match {
          case "boundary" => contentType = contentType.copy(boundary = value)
          case "charset" => contentType = contentType.copy(charSet = value)
          case _ => throw new RuntimeException(s"Unable to parse content type: [$contentTypeString]")
        }
      }
    }
    contentType
  } else {
    null
  }
}