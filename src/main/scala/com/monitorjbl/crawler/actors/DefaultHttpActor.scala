package com.monitorjbl.crawler.actors

import java.io.InputStream

class DefaultHttpActor extends HttpActor {
  ignoredPatterns = List(".*\\\\.(sha1)", ".*\\\\.(md5)")

  override def shouldBeRead(url: String, headers: Map[String, String]): Boolean = {
    return headers.getOrElse("Content-Type", "") == "text/xml"
  }

  override def read(url: String, headers: Map[String, String], content: InputStream): Unit = {
  }
}
