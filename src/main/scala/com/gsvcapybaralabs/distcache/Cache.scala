package com.gsvcapybaralabs.distcache

import akka.event.LoggingAdapter
import com.gsvcapybaralabs.distcache.eviction.{EvictionPolicy, LRU}

import scala.collection.mutable

/***
  * The cache implementation. This class is the main used by all slave nodes to maintain a local cache.
  * @param logger The logger
  * @param policy The eviction policy
  */
class Cache(logger: LoggingAdapter, policy: EvictionPolicy = LRU()) {
  private val availableSpace: Long = 3
  private val map: mutable.HashMap[String, CacheRecord] = new mutable.HashMap[String, CacheRecord]()
  private var currentSize: Long = 0

  /**
    * Gets an element from cache if exists
    * @param key
    * @return The element (if exists)
    */
  def get(key: String): Option[Any] = {
    val cacheRecord: Option[CacheRecord] = map.get(key)

    cacheRecord match {
      case Some(cacheRecord) =>
        cacheRecord.updateLastAccess()
      case _ => None
    }
    cacheRecord
  }

  /***
    * Puts an element to cache
    * @param key The key
    * @param value The value
    */
  def put(key: String, value: Any): Unit = {
    val record = CacheRecord(key=key, value=value, System.currentTimeMillis())

    if (currentSize + record.sizeInBytes > availableSpace){
      logger.info("No available cache space. Starting eviction process")
      freeSpace(record.sizeInBytes)
    }

    logger.info(s"Putting object with key $key to cache")
    map.put(key, record)
    currentSize += record.sizeInBytes
  }

  /**
    * Evicts an element from cache (if exists)
    * @param key The key
    * @return true in case of the key is found and evicted, false otherwise
    */
  def evict(key: String): Boolean = {
    logger.info(s"Evicting $key from cache")

    this.map.get(key) match {
      case Some(value) =>
        this.currentSize -= value.sizeInBytes
        this.map.remove(key)
        logger.info(s"$key removed successfully")

        true
      case _ =>
        logger.info(s"$key did not found in cache. Ignoring request")
        false
    }
  }

  /***
    * This method is invoked to free some space from cache, when it exceeds the
    * available capacity
    * @param size The size to be freed
    * @return true in case of success, false otherwise
    */
  def freeSpace(size: Long): Boolean = {
    logger.info(s"Freeing $size of cache space with policy: ${policy}")

    // Sort in descending order
    val sortedByRank = this.map.toList.sortBy(x => -this.policy.rank(x._2))

    var keysToEvict = mutable.Seq[String]()
    var spaceToBeFreed = 0

    var i =0
    while (spaceToBeFreed < size){
      val element = sortedByRank(i)._2
      spaceToBeFreed += element.sizeInBytes
      keysToEvict = keysToEvict :+ element.key
      i += 1
    }

    logger.info(s"Evicting the following keys: $keysToEvict")
    keysToEvict.foreach(evict)

    true
  }

  def getCacheContents = map.toList.sortBy(x => -this.policy.rank(x._2))

  def getAvailableSpace = availableSpace
  def getCurrentSize = currentSize
  def totalElementsInCache = map.size
}
