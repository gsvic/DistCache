package com.gsvcapybaralabs.distcache

import akka.cluster.{Cluster}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory


/***
  * This class represents the Slave node and its functionality,
  * defined in the receive function. receive function receives some message
  * from the master, line GetFromCache, PutToCache and so on, handles it and
  * returns the result back to the master.
  */
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
    case EvictFromCache(k) => {
      log.info(s"Received eviction request for key ${k}")
      cache.get(k) match {
        case Some(value) => {
          log.info(s"Found key $k in cache, evicting it")
          cache.evict(k)
          sender ! true
        }
        case _ => {
          log.info(s"Did not found key $k in cache")
          sender ! false
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

/***
  * The Slave's object and main method
  */
object Slave {
  def main(args: Array[String]) {
    val config = ConfigFactory.parseString(
      s"""akka.remote.netty.tcp.port=${args(0)}""").withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", config)
    val cache = system.actorOf(Props[Slave], name = "cache")
  }
}


