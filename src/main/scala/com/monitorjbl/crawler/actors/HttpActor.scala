package com.monitorjbl.crawler.actors

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import org.slf4j.{LoggerFactory, Logger}

import akka.actor.Actor
import com.monitorjbl.crawler.messages.{ReadComplete, ReadUrl}
import org.htmlcleaner.HtmlCleaner

import com.monitorjbl.crawler.HttpOperations.getStream
import com.monitorjbl.crawler.HttpOperations.head
import com.monitorjbl.crawler.HttpOperations.sanitizeUrl
import com.monitorjbl.crawler.HttpOperations.unrelativizeUrl

abstract class HttpActor extends Actor {
  val log: Logger = LoggerFactory.getLogger(classOf[HttpActor])
  val parseableContentTypes = List("text/html")
  var ignoredPatterns: List[String] = List()

  def load(url: String) = {
    log.trace("Reading " + url)
    //is path ignored?

    //check content type
    val headers = head(url).get

    //do stuff with content, if desired
    var content: RereadableInputStream = null
    if (shouldBeRead(url, headers)) {
      log.debug(s"Reading on $url")
      content = new RereadableInputStream(getStream(url).get)
      read(url, headers, content)
    } else {
      log.trace(s"Skipping read on $url")
    }

    //load links (if this content supports that)
    val contentType = headers.get("Content-Type")
    if (!parseableContentTypes.contains(contentType)) {
      if (content == null) {
        content = new RereadableInputStream(getStream(url).get)
      }
      content.reset()
      findLinks(url, content)
    } else {
      log.trace("Skipping link parsing on " + contentType)
    }
  }

  def findLinks(url: String, is: InputStream): Unit = {
    val cleaner = new HtmlCleaner
    val props = cleaner.getProperties

    val rootNode = cleaner.clean(is)
    val elements = rootNode.getElementsByName("a", true)
    for (elem <- elements) {
      val href = sanitizeUrl(unrelativizeUrl(url, elem.getAttributeByName("href")))
      if (href != null && !href.endsWith("../")) {
        sender ! ReadUrl(href)
      }
    }
  }

  /**
   * Overrideable method to determine if the current URL should be read from the server
   * @param headers
   * @param url
   * @return
   */
  def shouldBeRead(url: String, headers: Map[String, String]): Boolean

  /**
   * Overrideable method to do stuff with the content from the current URL
   * @param headers
   * @param content
   */
  def read(url: String, headers: Map[String, String], content: InputStream)

  def receive = {
    case ReadUrl(url) =>
      try {
        load(url)
      } catch {
        case e: Exception => log.error("Error during read", e)
      }

      log.trace(s"Read complete on $url")
      sender ! ReadComplete(url)
    case _ => log.warn("Unrecognize message type")
  }
}

/**
 * InputStream that copies all read values internally
 * @param inputStream
 */
class RereadableInputStream(inputStream: InputStream) extends InputStream {

  var bytes: Array[Byte] = null
  var bos: ByteArrayOutputStream = null
  var bis: ByteArrayInputStream = null

  override def read(): Int = {
    if (bos == null) {
      bos = new ByteArrayOutputStream()
    }

    var i = -1
    if (bis == null) {
      i = inputStream.read()
      bos.write(i)
    } else {
      i = bis.read()
    }
    return i
  }

  override def reset(): Unit = {
    //if the underlying stream has not been read, don't do anything
    if (bos == null) {
      return
    }

    // else, restart with new stream
    if (bytes == null) {
      bytes = bos.toByteArray
      bos = null
    } else {
      bis.close()
    }
    bis = new ByteArrayInputStream(bytes)
  }
}
