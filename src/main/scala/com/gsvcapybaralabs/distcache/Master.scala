package com.gsvcapybaralabs.distcache

import akka.pattern.ask

import scala.concurrent.duration._
import akka.cluster.{Cluster}
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.cluster.ClusterEvent.{MemberEvent, MemberUp, UnreachableMember}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

case class SendToActor(msg: Any, actor: String)
case class PutToCache(key: String, value: Any)
case class GetFromCache(key: String)
case class GetNodes()
case class ListCacheContents()

class Master extends Actor with ActorLogging {

  implicit val cluster = Cluster(context.system)
  implicit val timeout = Timeout(5 seconds)
  val cacheContext = new CacheContextImpl(cluster, context)



  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)



  def receive = {
    case MemberUp(member) => {
      if (!member.address.toString.equals("akka.tcp://ClusterSystem@127.0.0.1:2551"))
        this.cacheContext.memberUp(member)
    }

    case PutToCache(key, value) => {
      log.info(s"Placing (${key}, ${value}) to ${this.cacheContext.putToCache(key, value)}")
    }
    case GetFromCache(key) => {
      log.info(s"Requesting key $key from cache")
      val res = this.cacheContext.getFromCache(key)

      log.info(s"Got $res for key $key")
    }
    case SendToActor(msg, actor) => {
      log.info(s"Sending '${msg}' to ${actor}")
      context.actorSelection(actor) ? msg
    }
    case GetNodes() => {
      log.info("Listing current cluster members")
      this.cacheContext.getNodes.foreach(m => log.info(m.toString()))
    }
    case ListCacheContents() => {
      log.info("Listing cache contents to each node")
      cacheContext.idToMember.values.foreach{ m =>
        context.actorSelection(m.address + "/user/cache") ? ListCacheContents()
      }
    }
    case _ => {
      //log.error("Invalid request")
    }
  }



}

object Master extends App {

  //println(Math.abs("a".hashCode) % 5)

  override val args = Array[String]("2551")

  val config = ConfigFactory.parseString(
    s"""akka.remote.netty.tcp.port=${args(0)}""").withFallback(ConfigFactory.load())

  val system = ActorSystem("ClusterSystem", config)
  val cache = system.actorOf(Props[Master], name = "cache")

  implicit val timeout = Timeout(10 seconds)

  while (true) {
    print("Enter cmd: ")
    val cmd = scala.io.StdIn.readLine()

    cmd match {
      case "" => {}
      case "put" => {
        val keyVal = preparePutRequest

        cache ? PutToCache(keyVal._1, keyVal._2)
      }
      case "get" => {
        val key = prepareGetRequest
        cache ? GetFromCache(key)
      }
      case "nodes" => {
        cache ? GetNodes()
      }
      case "ls" => {
        cache ? ListCacheContents()
      }
      case _ => {}
    }

  }


  def preparePutRequest: (String, Any) = {
    print("Enter key name: ")
    val k = scala.io.StdIn.readLine()
    print("Enter value: ")
    val v = scala.io.StdIn.readLine()

    return (k, v)
  }

  def prepareGetRequest: (String) = {
    print("Enter key name: ")
    val k = scala.io.StdIn.readLine()

    return k
  }
}


