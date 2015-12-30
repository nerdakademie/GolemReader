package de.nerdakademie.golemreader.helper

import java.io.File
import android.content.{Context, SharedPreferences}
import android.os.Environment
import android.preference.PreferenceManager
import de.nerdakademie.golemreader.MainActivity

object FileCache {

  def deleteDir(dir: File): Boolean = {
    if (dir.isDirectory) {
      val children: Array[String] = dir.list
      for (aChildren <- children) {
        val success: Boolean = deleteDir(new File(dir, aChildren))
        if (!success) {
          return false
        }
      }
    }
    dir.delete
  }

  def getFolderSize(f: File): Long = {
    var size: Long = 0
    if (f.exists) {
      if (f.isDirectory) {
        for (file <- f.listFiles) {
          size += getFolderSize(file)
        }
      }
      else {
        size = f.length
      }
    }
    size / 1048576
  }
}

class FileCache {
  private var cacheDir: File = null

  def this(context: Context) {
    this()
    val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.MainActivityContext)
    if ((sp.getString("cacheFolder", "") == "sd") && (Environment.getExternalStorageState == Environment.MEDIA_MOUNTED)) {
      cacheDir = new File(Environment.getExternalStorageDirectory.toString + "/Golem/imageCache")
      val oldDir: File = new File(MainActivity.internalFileDir + "/imageCache")
      if (oldDir.list != null) {
        FileCache.deleteDir(oldDir)
      }
    }
    else if ((sp.getString("cacheFolder", "") == "internal") || !(Environment.getExternalStorageState == Environment.MEDIA_MOUNTED)) {
      val oldDir: File = new File(Environment.getExternalStorageDirectory.toString + "/Golem/imageCache")
      if (oldDir.list != null) {
        FileCache.deleteDir(oldDir)
      }
      cacheDir = new File(MainActivity.internalFileDir + "/imageCache")
    }
    else {
      cacheDir = new File(MainActivity.internalFileDir + "/imageCache")
    }
    if (!cacheDir.exists) {
      cacheDir.mkdirs
    }
    if (FileCache.getFolderSize(cacheDir) > 50) {
      FileCache.deleteDir(cacheDir)
    }
  }

  def getFile(url: String): File = {
    val filename: String = String.valueOf(url.hashCode)
    val f: File = new File(cacheDir, filename)
    f
  }

  def clear(): Unit = {
    val files: Array[File] = cacheDir.listFiles
    if (files == null) return
    for (f <- files) f.delete
  }
}