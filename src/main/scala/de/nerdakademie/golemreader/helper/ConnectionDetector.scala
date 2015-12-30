package de.nerdakademie.golemreader.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class ConnectionDetector {
  private[helper] var connectivity: ConnectivityManager = null
  private var _context: Context = null

  def this(context: Context) {
    this()
    this._context = context
  }

  def isConnectingToInternet: Boolean = {
    if (connectivity == null) {
      connectivity = _context.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    }
    if (connectivity != null) {
      val info: Array[NetworkInfo] = connectivity.getAllNetworkInfo
      if (info != null) for (anInfo <- info) if (anInfo.getState eq NetworkInfo.State.CONNECTED) {
        true
      }
    }
    false
  }

  def isMobile: Boolean = {
    if (connectivity == null) {
      connectivity = _context.getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    }
    val activeNetworkInfo: NetworkInfo = connectivity.getActiveNetworkInfo
    if (activeNetworkInfo != null) {
       activeNetworkInfo.getType == ConnectivityManager.TYPE_MOBILE
    }
    else {
      false
    }
  }
}

