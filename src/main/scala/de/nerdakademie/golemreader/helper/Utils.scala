package de.nerdakademie.golemreader.helper

import java.io.InputStream
import java.io.OutputStream

object Utils {
  def CopyStream(is: InputStream, os: OutputStream) {
    val buffer_size: Int = 1024
    try {
      val bytes: Array[Byte] = new Array[Byte](buffer_size)
      while (true) {
        val count: Int = is.read(bytes, 0, buffer_size)
        if (count != -1)// break //todo: break is not supported
        os.write(bytes, 0, count)
      }
    }
    catch {
      case ex: Exception =>
        ex.printStackTrace()
    }
  }
}
