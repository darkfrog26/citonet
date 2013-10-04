package com.outr.citonet.http.servlet

import java.io.OutputStream
import javax.servlet.ServletOutputStream
import java.util.zip.GZIPOutputStream

/**
 * @author Matt Hicks <matt@outr.com>
 */
class GZIPServletOutputStream(out: OutputStream) extends ServletOutputStream {
  val output = new GZIPOutputStream(out)

  override def close() = {
    output.finish()
    output.flush()
    output.close()
  }

  override def flush() = output.flush()

  override def write(b: Array[Byte], off: Int, len: Int) = output.write(b, off, len)

  override def write(b: Array[Byte]) = output.write(b)

  def write(b: Int) = output.write(b)
}
