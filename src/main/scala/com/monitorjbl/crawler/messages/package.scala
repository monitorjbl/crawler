package com.monitorjbl.crawler

package object messages {

  case class ReadUrl(url: String)

  case class ReadComplete(url: String)

  case class CrawlComplete(url: String)

}
