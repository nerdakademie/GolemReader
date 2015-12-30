package de.nerdakademie.golemreader

import java.io.File

import android.content.Context
import org.scaloid.common._

/**
  * Created by xilent on 29.12.15.
  */
object MainActivity{

  var KEY_ARTICLEID: String = "articleID"
  var KEY_HEADLINE: String = "headlne"
  var KEY_ABSTRACTTEXT: String = "abstracttext"
  var KEY_DATE: String = "date"
  var KEY_URL: String = "url"
  var KEY_LEADIMGURL: String = "leadimgurl"
  var KEY_LEADIMGURL_WIDTH: String = "leadimgurl_width"
  var KEY_LEADIMGURL_HEIGHT: String = "leadimgurl_height"
  var KEY_IMAGES: String = "images"
  var KEY_WIDTH: String = "width"
  var KEY_HEIGHT: String = "height"
  val readarticle: String = "com.xilent.golemreader.readarticle"
  val close: String = "com.xilent.golemreader.close"
  private var APIKEY: String = "1d77c5b958d491ac0b1ff034bbf844a0"
  var MainActivityContext: Context = null
  var internalFileDir: File = null

}


class MainActivity extends SActivity{


  onCreate{
    contentView = new SRelativeLayout {
      SListView().<<.fill.marginRight(3.dip).marginLeft(3.dip).>>.setAdapter(SArrayAdapter("One", "Two", "Three").dropDownStyle(_.textSize(25.dip)))
    }
    initVars()
  }


  def initVars(): Unit ={
    MainActivity.MainActivityContext = getApplicationContext
    MainActivity.internalFileDir = getFilesDir
  }

}
