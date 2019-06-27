package com.gsvcapybaralabs.distcache.cachecontext

import akka.actor.ActorContext
import akka.cluster.{Cluster, Member}
import com.gsvcapybaralabs.distcache.GetAllRecords
import com.gsvcapybaralabs.distcache.hashing.ConsistentHashing
import akka.pattern.ask


import scala.concurrent.Await
import scala.concurrent.duration._



class CacheContextImpl(cluster: Cluster, actorContext: ActorContext) extends BasicCacheContext(cluster, actorContext) {
  val consistentHashing: ConsistentHashing = new ConsistentHashing()

  def findNodeOld(key: String): Int = {
    val members = cluster.state.members
    val addresses = members.slice(1, members.size).map(_.address)
    val numbNodes = addresses.size

    val memberIdx = {
      val abs = Math.abs(key.hashCode)
      if (abs != 0) abs % numbNodes else abs
    }

    memberIdx
  }

  /**
    * findNode implementation with consistent hashing
    * @param key
    * @return The node id
    */
  override def findNode(key: String): Int = {
    consistentHashing.findNode(key)
  }

  override def memberUp(member: Member): Int = {
    val nodeId = super.memberUp(member)

    consistentHashing.addNode(nodeId)

    nodeId
  }
}