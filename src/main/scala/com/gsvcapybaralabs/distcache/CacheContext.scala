package com.gsvcapybaralabs.distcache

import akka.actor.ActorContext
import akka.cluster.{Cluster, Member}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.collection.mutable
import scala.collection.immutable
import scala.concurrent.Await

trait CacheContext{
  def putToCache(key: String, value: Any): String
  def getFromCache(key: String): Any
  def findNode(key: String): Int
}

abstract class BasicCacheContext(cluster: Cluster, actorContext: ActorContext) extends CacheContext {
  implicit val timeout = Timeout(20 seconds)

  val idToMember = mutable.HashMap[Int, Member]()
  val elementsPerNode = mutable.HashMap[Int, Long]()

  var id: Int = 0

  def getId = {
    val tmp = id;
    this.id = this.id + 1
    tmp
  }

  def putToCache(key: String, value: Any): String = {
    val nodeId = this.findNode(key)
    val nodeAddress = this.idToMember.get(nodeId) match {
      case Some(member) => member.address + "/user/cache"
      case _ => throw new Exception()
    }

    this.actorContext.actorSelection(nodeAddress) ? PutToCache(key, value)

    val nodeElements = this.elementsPerNode.get(nodeId).get + 1
    this.elementsPerNode.update(nodeId, nodeElements)

    nodeAddress
  }

  def getFromCache(key: String): Any = {
    val nodeId = this.findNode(key)
    val nodeAddress = this.idToMember.get(nodeId) match {
      case Some(member) => member.address + "/user/cache"
      case _ => throw new Exception()
    }

    val future = ask(actorContext.actorSelection(nodeAddress), GetFromCache(key))
    val res = Await.result(future, timeout.duration)

    return res
  }

  def memberUp(member: Member): Unit = {
    val nodeId = getId
    this.idToMember.put(nodeId, member)
    this.elementsPerNode.put(nodeId, 0)
  }

  def getNodes: immutable.SortedSet[Member] = {
    this.cluster.state.members
  }

}
