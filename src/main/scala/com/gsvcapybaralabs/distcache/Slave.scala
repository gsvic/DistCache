package com.gsvcapybaralabs.distcache

import akka.cluster.{Cluster}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory


class Slave extends Actor with ActorLogging {
  private val cluster = Cluster(context.system)
  private val cache = new Cache(log)

  def receive = {
    case PutToCache(k, v) => {
      log.info(s"Received ${k}, ${v}")
      val record = CacheRecord(key=k, value=v, System.currentTimeMillis())
      cache.put(k, record)
    }
    case GetFromCache(k) => {
      log.info(s"Received request for key ${k}")
      cache.get(k) match {
        case Some(value) => {
          log.info(s"Found key $k in cache")
          sender ! value
        }
        case _ => {
          log.info(s"Did not found key $k in cache")
          sender ! None
        }
      }
    }
    case ListCacheContents() => {
      log.info("Listing cache contents")
      cache.getCacheContents.toList.foreach(element => log.info(element.toString()))
      log.info(s"Total space: ${cache.getAvailableSpace}, Allocated space: ${cache.getCurrentSize}, " +
        s"Total elements: ${cache.totalElementsInCache}")
    }
    case  GetAllRecords() => {
      sender ! cache.getCacheContents.map(_._2).toSet
    }
  }


}

object Slave {
  def main(args: Array[String]) {
    val config = ConfigFactory.parseString(
      s"""akka.remote.netty.tcp.port=${args(0)}""").withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    val cache = system.actorOf(Props[Slave], name = "cache")
  }
}


