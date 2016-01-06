package de.nerdakademie.golemreader.helper

import android.util.Log
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import java.io._
import java.net.HttpURLConnection
import java.net.URL

class XMLParser {

  /**
    * Getting XML from URL making HTTP request
    * @param url string
    **/
  def getXmlFromUrl(url: String): String = {
    var xml: String = ""
    try {
      val xmlurl: URL = new URL(url)
      val connection: HttpURLConnection = xmlurl.openConnection.asInstanceOf[HttpURLConnection]
      connection.setDoOutput(true)
      connection.setDoInput(true)
      connection.setInstanceFollowRedirects(true)
      val br: BufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream))
      var line: String = null
      while ((({
        line = br.readLine; line
      })) != null) {
        xml += line
      }
      br.close()
      connection.disconnect()
    }
    catch {
      case e: IOException =>
        e.printStackTrace()

    }
    xml
  }

  def getDomElement(xml: String): Document = {
    var doc: Document = null
    val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance
    try {
      val db: DocumentBuilder = dbf.newDocumentBuilder
      val is: InputSource = new InputSource
      is.setCharacterStream(new StringReader(xml))
      doc = db.parse(is)
    }
    catch {
      case e: ParserConfigurationException => {
        Log.e("Error: ", e.getMessage)
        return null
      }
      case e: SAXException => {
        Log.e("Error: ", e.getMessage)
        return null
      }
      case e: IOException => {
        Log.e("Error: ", e.getMessage)
        return null
      }
    }
     doc
  }

  /** Getting node value
    * @param elem element
    */
  final def getElementValue(elem: Node): String = {
    var child: Node = null
    if (elem != null) {
      if (elem.hasChildNodes) {
        {
          child = elem.getFirstChild
          while (child != null) {
            {
              if (child.getNodeType == Node.TEXT_NODE) {
                return child.getNodeValue
              }
            }
            child = child.getNextSibling
          }
        }
      }
    }
    ""
  }

  def getValue(item: Element, str: String): String = {
    val n: NodeList = item.getElementsByTagName(str)
    this.getElementValue(n.item(0))
  }

  def getValueCustom(item: Element, str: String): String = {
    val n: NodeList = item.getElementsByTagName(str)
    this.getElementValue(n.item(1))
  }
}
