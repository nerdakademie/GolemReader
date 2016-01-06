package de.nerdakademie.golemreader

import java.io.File

import android.app.{AlertDialog, ProgressDialog, ActivityManager}
import android.content.{DialogInterface, Intent, SharedPreferences, Context}
import android.os.{Message, Looper}
import android.preference.PreferenceManager
import android.util.Log
import de.nerdakademie.golemreader.helper.{XMLParser, ConnectionDetector}
import org.scaloid.common._
import org.w3c.dom.{Element, NodeList, Document}

/**
  * Created by xilent on 29.12.15.
  */
object MainActivity {
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
  val readarticle: String = "de.nerdakademie.golemreader.readarticle"
  val close: String = "de.nerdakademie.golemreader.close"
  private val STEP_NULL_COMPLETE: Int = 3
  private val STEP_ONE_COMPLETE: Int = 0
  private val STEP_TWO_COMPLETE: Int = 1
  private val STEP_THREE_COMPLETE: Int = 2

}


class MainActivity extends SActivity{
  private val APIKEY: String = "1d77c5b958d491ac0b1ff034bbf844a0"
  var MainActivityContext: Context = getApplicationContext
  var internalFileDir: File = getFilesDir
  private[golemreader] var pd: ProgressDialog = null
  private var data: java.util.ArrayList[java.util.HashMap[String, String]] = null
  var sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
  private[golemreader] var cd: ConnectionDetector = new ConnectionDetector()
  private[golemreader] var internet: Boolean = false
  private[golemreader] var mobile: Boolean = false
  var newestArt: Int = 0


  onCreate{
    contentView = new SRelativeLayout {
      SListView().<<.fill.marginRight(3.dip).marginLeft(3.dip).>>.setAdapter(SArrayAdapter("One", "Two", "Three").dropDownStyle(_.textSize(25.dip)))
    }
  }


  private def setsharedPreferences(): Unit = {
    if (sp.getInt("configured", 0) == 0) {
      val editor: SharedPreferences.Editor = sp.edit
      editor.putBoolean("auto_refresh", true)
      editor.putBoolean("vibrate", true)
      editor.putBoolean("ton", true)
      editor.putInt("configured", 1)
      editor.putString("update", "5")
      editor.putBoolean("mainpictures", true)
      editor.putBoolean("articlepictures", true)
      editor.putBoolean("scrolldown", true)
      editor.putString("cacheFolder", "sd")
      editor.putString("vibration_duration", "200")
      editor.apply()
    }
  }

  private def isMyServiceRunning(serviceClass: Class[_]): Boolean = {
    val manager: ActivityManager = getSystemService(Context.ACTIVITY_SERVICE).asInstanceOf[ActivityManager]
    import scala.collection.JavaConversions._
    for (service <- manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName == service.service.getClassName) {
        return true
      }
    }
    false
  }

  def loaddata(): Unit = {
    internet = cd.isConnectingToInternet
    Log.d("Internet State", "Internet = " + internet)
    mobile = cd.isMobile
    if (internet) {
      pd = new ProgressDialog(this)
      pd.setMessage("Laden...")
      pd.setCancelable(false)
      pd.setIndeterminate(true)
      pd.show()
      new Thread(new Runnable() {
        def run {
          Looper.prepare()
          try {
            newestArt = 0
            data = new java.util.ArrayList[java.util.HashMap[String, String]]
            val parser: XMLParser = new XMLParser
            val xml: String = parser.getXmlFromUrl("http://api.golem.de/api/article/latest/50/?key=" + APIKEY + "&format=xml")
            val doc: Document = parser.getDomElement(xml)
            if (xml.contains("success")) {
              val nl: NodeList = doc.getElementsByTagName("golemderesult")
              {
                var i: Int = 0
                while (i < nl.getLength) {
                  {
                    val e: Element = nl.item(i).asInstanceOf[Element]
                    val success: Int = Integer.valueOf(parser.getValue(e, "success"))
                    if (success != 1) {
                      Log.i(getClass.getSimpleName, "MainActivity: No Success while fetching!")
                      return
                    }
                    val item: NodeList = e.getElementsByTagName("data")
                    {
                      var j: Int = 0
                      while (j < item.getLength) {
                        {
                          val f: Element = item.item(j).asInstanceOf[Element]
                          val records: NodeList = f.getElementsByTagName("record")
                          {
                            var k: Int = 0
                            while (k < records.getLength) {
                              {
                                val g: Element = records.item(k).asInstanceOf[Element]
                                val map: java.util.HashMap[String, String] = new java.util.HashMap[String, String]
                                map.put(MainActivity.KEY_ARTICLEID, parser.getValue(g, "articleid"))
                                if (map.get(MainActivity.KEY_ARTICLEID) != null) {
                                  if (Integer.valueOf(map.get(MainActivity.KEY_ARTICLEID)) > newestArt) {
                                    newestArt = Integer.valueOf(map get MainActivity.KEY_ARTICLEID)
                                  }
                                }
                                map.put(MainActivity.KEY_HEADLINE, parser.getValue(g, "headline"))
                                map.put(MainActivity.KEY_ABSTRACTTEXT, parser.getValue(g, "artist"))
                                map.put(MainActivity.KEY_DATE, parser.getValue(g, "date"))
                                map.put(MainActivity.KEY_URL, parser.getValue(g, "url"))
                                val LeadImage: NodeList = g.getElementsByTagName("leadimg")
                                {
                                  var l: Int = 0
                                  while (l < LeadImage.getLength) {
                                    {
                                      val h: Element = LeadImage.item(l).asInstanceOf[Element]
                                      map.put(MainActivity.KEY_LEADIMGURL, parser.getValue(h, "url"))
                                      map.put(MainActivity.KEY_LEADIMGURL_HEIGHT, parser.getValue(h, "height"))
                                      map.put(MainActivity.KEY_LEADIMGURL_WIDTH, parser.getValue(h, "width"))
                                    }
                                    ({
                                      l += 1; l - 1
                                    })
                                  }
                                }
                                map.put(MainActivity.KEY_IMAGES, parser.getValue(g, "images"))
                                data.add(map)
                              }
                              ({
                                k += 1; k - 1
                              })
                            }
                          }
                        }
                        ({
                          j += 1; j - 1
                        })
                      }
                    }
                  }
                  ({
                    i += 1; i - 1
                  })
                }
              }
              val intent: Intent = new Intent
              intent.putExtra("artID", String.valueOf(newestArt))
              intent.setAction(MainActivity.readarticle)
              sendBroadcast(intent)
              Log.d("Handler", "Firing InitUI Handler")
              val msg: Message = Message.obtain
              msg.what = MainActivity.STEP_ONE_COMPLETE
              handler.sendMessage(msg)
            }
            else {
              if (pd.isShowing) {
                pd.dismiss()
              }
              val msg: Message = Message.obtain
              msg.what = MainActivity.STEP_TWO_COMPLETE
              handler.sendMessage(msg)
            }
          }
          catch {
            case e: Exception => {
              e.printStackTrace()
              showAlertDialog(MainActivity.this, "Fehler", "Bei der Abfrage der Daten ist etwas schiefgelaufen")
            }
          }
        }
      }).start()
    }
    else {
      showAlertDialog(this, "Fehler", "Keine Internetverbindung vorhanden. Bitte stellen sie eine solche her")
    }
  }

  def showAlertDialog(context: Context, title: String, message: String) {
    var alertDialog: AlertDialog = null
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
      alertDialog = new AlertDialog.Builder(this, android.R.style.Theme_Holo_Dialog).create
    }
    else {
      alertDialog = new AlertDialog.Builder(context).create
    }
    alertDialog.setTitle(title)
    alertDialog.setMessage(message)
    alertDialog.setIcon(R.drawable.golem_eye)
    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Erneut versuchen", new DialogInterface.OnClickListener() {
      def onClick(dialog: DialogInterface, which: Int) {
        loaddata
        dialog.dismiss()
      }
    })
    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Abbrechen", new DialogInterface.OnClickListener() {
      def onClick(dialog: DialogInterface, which: Int) {
        MainActivity.this.finish()
        dialog.dismiss()
      }
    })
    alertDialog.show()
  }

}
