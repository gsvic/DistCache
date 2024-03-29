package com.gsvcapybaralabs.distcache.cachecontext

import akka.actor.ActorContext
import akka.cluster.{Cluster, Member}
import akka.pattern.ask
import akka.util.Timeout
import com.gsvcapybaralabs.distcache.{EvictFromCache, GetFromCache, PutToCache}

import scala.concurrent.duration._
import scala.collection.mutable
import scala.collection.immutable
import scala.concurrent.Await


/**
  * The basic cache context implementation
  * @param cluster
  * @param actorContext
  */
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

  override def putToCache(key: String, value: Any): String = {
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

  override def getFromCache(key: String): Any = {
    val nodeId = this.findNode(key)
    val nodeAddress = this.idToMember.get(nodeId) match {
      case Some(member) => member.address + "/user/cache"
      case _ => throw new Exception()
    }

    val future = ask(actorContext.actorSelection(nodeAddress), GetFromCache(key))
    val res = Await.result(future, timeout.duration)

    return res
  }

  override def evictFromCache(key: String): Any = {
    val nodeId = this.findNode(key)
    val nodeAddress = this.idToMember.get(nodeId) match {
      case Some(member) => member.address + "/user/cache"
      case _ => throw new Exception()
    }

    val future = ask(actorContext.actorSelection(nodeAddress), EvictFromCache(key))
    val res = Await.result(future, timeout.duration)

    return res
  }

  def memberUp(member: Member): Int = {
    val nodeId = getId
    this.idToMember.put(nodeId, member)
    this.elementsPerNode.put(nodeId, 0)

    nodeId
  }

  def getNodes: immutable.SortedSet[Member] = {
    this.cluster.state.members
  }

}
