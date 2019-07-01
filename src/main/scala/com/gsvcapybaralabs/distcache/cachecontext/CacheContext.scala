package com.gsvcapybaralabs.distcache.cachecontext

/***
  * The basic abstraction for the caching context, defined by four methods.
  */
trait CacheContext{
  def putToCache(key: String, value: Any): String
  def getFromCache(key: String): Any
  def evictFromCache(key: String): Any
  def findNode(key: String): Int
}