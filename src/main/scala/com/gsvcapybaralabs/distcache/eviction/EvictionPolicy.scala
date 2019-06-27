package com.gsvcapybaralabs.distcache.eviction

import com.gsvcapybaralabs.distcache.CacheRecord

/**
  * This class contains all the ranking functions for our available eviction policies
  */
trait EvictionPolicy {
  def rank(cacheRecord: CacheRecord): Double
}

case class LRU() extends EvictionPolicy {
  override def rank(cacheRecord: CacheRecord): Double = -cacheRecord.lastAccessed
}

case class MRU() extends EvictionPolicy {
  override def rank(cacheRecord: CacheRecord): Double = cacheRecord.lastAccessed
}

case class LFU() extends EvictionPolicy {
  override def rank(cacheRecord: CacheRecord): Double = -cacheRecord.numRequests
}

case class MFU() extends EvictionPolicy {
  override def rank(cacheRecord: CacheRecord): Double = cacheRecord.numRequests
}