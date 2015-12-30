package de.nerdakademie.golemreader.helper

import java.lang.ref.SoftReference
import java.util.Collections
import android.graphics.Bitmap

class MemoryCache {
  private val cache: java.util.Map[String, SoftReference[Bitmap]] = Collections.synchronizedMap(new java.util.HashMap[String, SoftReference[Bitmap]])

  def get(id: String): Bitmap = {
    if (!cache.containsKey(id)) return null
    val ref: SoftReference[Bitmap] = cache.get(id)
    ref.get
  }

  def put(id: String, bitmap: Bitmap) {
    cache.put(id, new SoftReference[Bitmap](bitmap))
  }

  def clear(): Unit = {
    cache.clear()
  }
}