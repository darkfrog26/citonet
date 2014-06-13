package com.outr.net.http.mime

import scala.io.Source
import java.io.File
import java.nio.file.Files

/**
 * @author Matt Hicks <matt@outr.com>
 */
object MimeType {
  val LineRegex = """(\S+)\t*(.*)""".r

  private var map = Map.empty[String, String]

  load()

  def add(extension: String, mimeType: String) = synchronized {
    map += extension.toLowerCase -> mimeType.toLowerCase
  }

  def lookup(extension: String, default: String) = if (extension != null) {
    map.get(extension.toLowerCase) match {
      case Some(m) => m
      case None => default
    }
  } else {
    default
  }

  def fromFilename(filename: String, default: String): Option[String] = if (filename.indexOf('.') != -1) {
    Option(lookup(filename.substring(filename.lastIndexOf('.') + 1), default))
  } else {
    None
  }

  def fromFile(file: File): Option[String] = fromFilename(file.getName, Files.probeContentType(file.toPath))

  // Load mime types from file
  private def load() = {
    val source = Source.fromURL(getClass.getClassLoader.getResource("mime_types.txt"))
    try {
      source.getLines().foreach {
        case line if line.trim.startsWith("#") => // Ignore commented out lines
        case LineRegex(mimeType, extension) => {
          extension.split(' ').foreach {
            case ext => add(ext, mimeType)
          }
        }
      }
    } finally {
      source.close()
    }
  }
}