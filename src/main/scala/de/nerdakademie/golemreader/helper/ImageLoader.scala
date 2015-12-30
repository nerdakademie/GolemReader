package de.nerdakademie.golemreader.helper

import java.io._
import java.net.{HttpURLConnection, URL}
import java.util.Collections
import java.util.concurrent.{ExecutorService, Executors}

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.{Bitmap, BitmapFactory}
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.widget.ImageView
import de.nerdakademie.golemreader.R

class ImageLoader {
  private[helper] var memoryCache: MemoryCache = new MemoryCache
  private[helper] var fileCache: FileCache = null
  private val imageViews: java.util.Map[ImageView, String] = Collections.synchronizedMap(new java.util.WeakHashMap[ImageView, String])
  private[helper] var executorService: ExecutorService = null

  def this(context: Context) {
    this()
    fileCache = new FileCache(context)
    executorService = Executors.newFixedThreadPool(5)
  }

  private[helper] final val stub_id: Int = R.drawable.golem_eye
  private[helper] final val transparent: Int = R.drawable.transparent

  def DisplayImage(url: String, imageView: ImageView) {
    imageViews.put(imageView, url)
    val bitmap: Bitmap = memoryCache.get(url)
    if (bitmap != null) {
      imageView.setImageBitmap(bitmap)
    }
    else {
      queuePhoto(url, imageView)
      imageView.setImageResource(stub_id)
    }
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1) def CheckBitmapSize(URL: String): Int = {
    var url: URL = null
    try {
      url = new URL(URL)
    }
    catch {
      case e: Exception =>
        e.printStackTrace()

    }
    val bitmap: Bitmap = noCache(url)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
      bitmap.getRowBytes * bitmap.getHeight
    }
    else {
      bitmap.getByteCount
    }
  }

  def DisplayImageTransparent(url: String, imageView: ImageView) {
    imageViews.put(imageView, url)
    val bitmap: Bitmap = memoryCache.get(url)
    if (bitmap != null) imageView.setImageBitmap(bitmap)
    else {
      queuePhoto(url, imageView)
      imageView.setImageResource(transparent)
    }
  }

  @SuppressWarnings(Array("deprecation")) def DisplayDrawable(url: String): BitmapDrawable = {
    var bitmap: Bitmap = memoryCache.get(url)
    if (bitmap != null)  new BitmapDrawable(bitmap)
    else {
      bitmap = getBitmap(url)
      memoryCache.put(url, bitmap)
      new BitmapDrawable(bitmap)
    }
  }

  private def queuePhoto(url: String, imageView: ImageView) {
    val p: PhotoToLoad = new PhotoToLoad(url, imageView)
    executorService.submit(new PhotosLoader(p))
  }

  def getBitmap(url: String): Bitmap = {
    val f: File = fileCache.getFile(url)
    try {
      val b: Bitmap = decodeFile(f)
      if (b != null) {
        b
      }
      else {
        val imageUrl: URL = new URL(url)
        val conn: HttpURLConnection = imageUrl.openConnection.asInstanceOf[HttpURLConnection]
        conn.setConnectTimeout(30000)
        conn.setReadTimeout(30000)
        conn.setInstanceFollowRedirects(true)
        val is: InputStream = conn.getInputStream
        val os: OutputStream = new FileOutputStream(f)
        Utils.CopyStream(is, os)
        os.close
        decodeFile(f)
      }
    }
    catch {
      case ex: Exception =>
        ex.printStackTrace()
        null

    }
  }

  def noCache(url: URL): Bitmap = {
    try {
      val connection: HttpURLConnection = url.openConnection.asInstanceOf[HttpURLConnection]
      connection.setDoOutput(true)
      connection.setDoInput(true)
      connection.setInstanceFollowRedirects(true)
      BitmapFactory.decodeStream(connection.getInputStream)
    }
    catch {
      case e: Exception =>
        e.printStackTrace()

    }
    null
  }

  private def decodeFile(f: File): Bitmap = {
    try {
      val o: BitmapFactory.Options = new BitmapFactory.Options
      o.inJustDecodeBounds = true
      BitmapFactory.decodeStream(new FileInputStream(f), null, o)
      val REQUIRED_SIZE: Int = 70
      var width_tmp: Int = o.outWidth
      var height_tmp: Int = o.outHeight
      var scale: Int = 1
      while (true) {
        if (!(width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)) {
          //todo: break is not supported
          width_tmp /= 2
          height_tmp /= 2
          scale *= 2
        }
      }
      val o2: BitmapFactory.Options = new BitmapFactory.Options
      o2.inSampleSize = scale
      o2.inPurgeable = true
      o2.inInputShareable = true
      return BitmapFactory.decodeStream(new FileInputStream(f))
    }
    catch {
      case e: FileNotFoundException =>
        e.printStackTrace()

    }
     null
  }

  private class PhotoToLoad {
    var url: String = null
    var imageView: ImageView = null

    def this(u: String, i: ImageView) {
      this()
      url = u
      imageView = i
    }
  }

  class PhotosLoader extends Runnable {
    private[helper] var photoToLoad: PhotoToLoad = null

    private[helper] def this(photoToLoad: PhotoToLoad) {
      this()
      this.photoToLoad = photoToLoad
    }

    def run() {
      if (imageViewReused(photoToLoad)) return
      val bmp: Bitmap = getBitmap(photoToLoad.url)
      memoryCache.put(photoToLoad.url, bmp)
      if (imageViewReused(photoToLoad)) return
      val bd: BitmapDisplayer = new BitmapDisplayer(bmp, photoToLoad)
      val a: Activity = photoToLoad.imageView.getContext.asInstanceOf[Activity]
      a.runOnUiThread(bd)
    }
  }

  private[helper] def imageViewReused(photoToLoad: ImageLoader#PhotoToLoad): Boolean = {
    val tag: String = imageViews.get(photoToLoad.imageView)
    tag == null || !(tag == photoToLoad.url)
  }

  private[helper] class BitmapDisplayer extends Runnable {
    private[helper] var bitmap: Bitmap = null
    private[helper] var photoToLoad: ImageLoader#PhotoToLoad = null

    def this(b: Bitmap, p: ImageLoader#PhotoToLoad) {
      this()
      bitmap = b
      photoToLoad = p
    }

    def run() {
      if (imageViewReused(photoToLoad)) return
      if (bitmap != null) photoToLoad.imageView.setImageBitmap(bitmap)
      else photoToLoad.imageView.setImageResource(stub_id)
    }
  }

  def clearCache():Unit = {
    memoryCache.clear()
    fileCache.clear()
  }
}


