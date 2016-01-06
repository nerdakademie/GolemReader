package de.nerdakademie.golemreader

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date

import de.nerdakademie.golemreader.helper.{ImageLoader, ConnectionDetector}

object ListAdapter {
  private var inflater: LayoutInflater = null

  private class ViewHolder {
    var iconView: ImageView = null
    var headline: TextView = null
    var date: TextView = null
  }

}

class ListAdapter extends BaseAdapter {
  private[golemreader] var activity: Activity = null
  private var data: java.util.ArrayList[java.util.HashMap[String, String]] = null
  private[golemreader] var mobile: Boolean = false
  private[golemreader] var cd: ConnectionDetector = null
  private[golemreader] var sp: SharedPreferences = null
  private[golemreader] var imageLoader: ImageLoader = null

  def this(a: Activity, d: java.util.ArrayList[java.util.HashMap[String, String]]) {
    this()
    activity = a
    data = d
    imageLoader = new ImageLoader(a.getApplicationContext)
    cd = new ConnectionDetector(a.getApplicationContext)
    ListAdapter.inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
    sp = PreferenceManager.getDefaultSharedPreferences(a)
  }

  def getCount: Int = {
    data.size
  }

  def getItem(position: Int): Int = {
    position
  }

  def getItemId(position: Int): Long = {
    position
  }

  def updateData(newData: java.util.ArrayList[java.util.HashMap[String, String]]) {
    data = newData
    notifyDataSetChanged()
  }

  def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    var holder: ListAdapter.ViewHolder = null
    var newView: View = null
    mobile = cd.isMobile
    if (convertView == null) {
      newView = activity.getLayoutInflater.inflate(R.layout.listgroup, parent, false)
      val header: TextView = convertView.findViewById(R.id.header).asInstanceOf[TextView]
      header.setTypeface(null, Typeface.BOLD)
      val description: TextView = convertView.findViewById(R.id.desc).asInstanceOf[TextView]
      val thumb_image: ImageView = convertView.findViewById(R.id.icon).asInstanceOf[ImageView]
      holder = new ListAdapter.ViewHolder
      holder.iconView = thumb_image
      holder.headline = header
      holder.date = description
      newView.setTag(holder)
    }
    else {
      holder = convertView.getTag.asInstanceOf[ListAdapter.ViewHolder]
    }
    var info: java.util.HashMap[String, String] = new java.util.HashMap[String, String]
    info = data.get(position)
    holder.headline.setText(info.get(MainActivity.KEY_HEADLINE))
    val dv: Long = info.get(MainActivity.KEY_DATE).toLong * 1000
    val df: Date = new Date(dv)
    val vv: String = new SimpleDateFormat("HH:mm dd.MM.yyyy").format(df)
    holder.date.setText(vv)
    holder.iconView.setTag(info.get(MainActivity.KEY_LEADIMGURL))
    if (sp.getBoolean("mainpictures", true) || !mobile) {
     imageLoader.DisplayImage(info.get(MainActivity.KEY_LEADIMGURL), holder.iconView)
    }
    else {
      holder.iconView.setScaleType(ImageView.ScaleType.CENTER_CROP)
      holder.iconView.setBackgroundResource(R.drawable.golem_eye)
    }
    if(newView == null){
      convertView
    }else{
      newView
    }

  }
}
