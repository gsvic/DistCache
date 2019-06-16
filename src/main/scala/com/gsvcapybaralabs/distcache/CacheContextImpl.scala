package com.gsvcapybaralabs.distcache

import akka.actor.ActorContext
import akka.cluster.Cluster

class CacheContextImpl(cluster: Cluster, actorContext: ActorContext) extends BasicCacheContext(cluster, actorContext) {

  override def findNode(key: String): Int = {
    val members = cluster.state.members
    val addresses = members.slice(1, members.size).map(_.address)
    val numbNodes = addresses.size

    val memberIdx = {
      val abs = Math.abs(key.hashCode)
      if (abs != 0) abs % numbNodes else abs
    }

    memberIdx
  }
}
