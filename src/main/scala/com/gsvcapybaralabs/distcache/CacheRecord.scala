package com.gsvcapybaralabs.distcache

/***
  * Represents a record in cache, along with its metadata
  * @param key The key
  * @param value The value
  * @param lastAccessed The last access timestamp
  * @param numRequests The total number of requests
  */
case class CacheRecord(key: String, value: Any, var lastAccessed: Long, var numRequests: Long = 0){
  val sizeInBytes = 1
  def updateLastAccess(): Unit = {
    lastAccessed = System.currentTimeMillis()
    numRequests += 1
  }
}