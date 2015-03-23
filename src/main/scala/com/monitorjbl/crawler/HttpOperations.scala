package com.monitorjbl.crawler

import java.io.InputStream

import org.apache.http.client.HttpClient
import org.apache.http.client.methods.{HttpGet, HttpHead}
import org.apache.http.impl.client.HttpClientBuilder

import scala.reflect.ClassTag

object HttpOperations {
  val retries = 3
  val http: HttpClient = HttpClientBuilder.create().build()
  type retryableRead = () => Option[Any]

  def head(url: String): Option[Map[String, String]] = {
    return retryReadOperation(() => {
      val response = http.execute(new HttpHead(url))
      if (response.getStatusLine.getStatusCode != 200)
        throw new OperationFailedException("Got response code " + response.getStatusLine.getStatusCode)
      return Option(response.getAllHeaders.map(h => h.getName -> h.getValue).toMap)
    })
  }

  def getStream(url: String): Option[InputStream] = {
    return retryReadOperation(() => {
      val response = http.execute(new HttpGet(url))
      if (response.getStatusLine.getStatusCode != 200)
        throw new OperationFailedException("Got response code " + response.getStatusLine.getStatusCode)
      return Option(response.getEntity.getContent)
    })
  }

  def retryReadOperation[T: ClassTag](operation: retryableRead): Option[T] = {
    var ret: Option[T] = None
    (0 until retries).toStream.takeWhile(_ => ret == None).foreach(i => {
      try {
        ret = operation().asInstanceOf[Option[T]]
      } catch {
        case e: OperationFailedException =>
          println(e.getMessage)
        case e: Exception =>
          e.printStackTrace()
      }
    })
    return ret
  }

  def sanitizeUrl(url: String): String = {
    if (url == null)
      return null
    else
      return url.replaceAll("(?<!http:)//", "/")
  }

  def unrelativizeUrl(baseUrl: String, relative: String): String = {
    if (relative == null || baseUrl == null)
      return null
    else if (relative.startsWith("http://"))
      return relative
    else
      return baseUrl + "/" + relative
  }
}

class OperationFailedException(msg: String) extends RuntimeException(msg)
