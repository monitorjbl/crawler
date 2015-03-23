package com.monitorjbl

import com.monitorjbl.crawler.{HttpOperations, Crawler}
import org.junit.Test

class CrawlerTest {

  @Test
  def test(): Unit = {
    val crawler = new Crawler("http://repo1.maven.org/maven2/org/apache/directory/api/api-all/", 5, 1)
    crawler.crawl()
  }

  @Test
  def testHead(): Unit = {
    val headers = HttpOperations.head("http://central.maven.org/maven2/org/eclipse/jetty/jetty-server")
    println(headers match {
      case Some(value) => value
      case None => throw new Exception("")
    })
  }
}
