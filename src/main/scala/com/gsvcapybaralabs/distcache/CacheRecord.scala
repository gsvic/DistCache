package com.gsvcapybaralabs.distcache

case class CacheRecord(key: String, value: Any, var lastAccessed: Long, var numRequests: Long = 0){
  val sizeInBytes = 1
  def updateLastAccess(): Unit = {
    lastAccessed = System.currentTimeMillis()
    numRequests += 1
  }
}