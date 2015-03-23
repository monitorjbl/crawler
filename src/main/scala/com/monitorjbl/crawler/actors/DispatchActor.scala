package com.monitorjbl.crawler.actors

import java.util.concurrent.atomic.AtomicLong

import akka.actor.{Props, Actor}
import akka.actor.Actor.Receive
import com.monitorjbl.crawler.messages.{CrawlComplete, ReadComplete, ReadUrl}
import org.slf4j.{LoggerFactory, Logger}

class DispatchActor(val id: String, val actor: Class[_ <: HttpActor]) extends Actor {
  val log: Logger = LoggerFactory.getLogger(classOf[DispatchActor])
  val currentlyReading: AtomicLong = new AtomicLong(0)

  def read(url: String): Unit = {
    log.trace("Dispatching " + url)
    context.system.actorOf(Props(actor.newInstance())) ! ReadUrl(url)
  }

  def receive = {
    case ReadUrl(url) =>
      currentlyReading.incrementAndGet()
      read(url)
    case ReadComplete(url) =>
      val count = currentlyReading.decrementAndGet()
      if (count == 0)
        context.system.eventStream.publish(CrawlComplete(id))
      else
        log.trace("count is " + count)
    case _ => println("Unknown message")
  }
}
