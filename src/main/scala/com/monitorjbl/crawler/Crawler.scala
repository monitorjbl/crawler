package com.monitorjbl.crawler

import java.util.UUID

import akka.actor.{Actor, Props, ActorSystem}
import com.monitorjbl.crawler.actors.{DefaultHttpActor, DispatchActor}
import com.monitorjbl.crawler.messages.ReadUrl
import com.monitorjbl.crawler.messages.CrawlComplete
import com.typesafe.config.ConfigFactory
import org.slf4j.{LoggerFactory, Logger}

class Crawler(val startUrl: String, val threads: Int = 10, val threadsPerCore: Int = 1) {
  val log: Logger = LoggerFactory.getLogger(classOf[Crawler])

  def crawl(): Unit = {
    val customConf = ConfigFactory.parseString(
      s"""
         |akka {
         |#log-config-on-start = on
         |actor{
         | default-dispatcher {
         |   type = "Dispatcher"
         |   executor = "fork-join-executor"
         |   throughput = 100
         |   fork-join-executor {
         |     parallelism-min = $threads
          |     parallelism-max = $threads
          |     parallelism-factor = $threadsPerCore

          |   }
          | }
          |}
          |}
      """.stripMargin)
    var system = ActorSystem("Crawler", ConfigFactory.load(customConf))

    //construct dispatcher
    val requestId = UUID.randomUUID.toString
    val dispatcher = system.actorOf(Props(new DispatchActor(requestId, classOf[DefaultHttpActor])), name = "dispatchActor")
    dispatcher ! ReadUrl(startUrl)
    log.debug(s"Starting request $requestId")

    //register completion event listener
    var complete = false
    val listener = system.actorOf(Props(new Actor {
      def receive = {
        case CrawlComplete(d) => {
          if (requestId == d) {
            log.debug(s"Request $d complete")
            complete = true
          }
        }
      }
    }))
    system.eventStream.subscribe(listener, classOf[CrawlComplete])

    while (!complete) {
      Thread.sleep(100)
    }

    system.shutdown()
  }


}