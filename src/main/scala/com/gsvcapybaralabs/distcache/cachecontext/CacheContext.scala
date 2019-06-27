package com.gsvcapybaralabs.distcache.cachecontext

trait CacheContext{
  def putToCache(key: String, value: Any): String
  def getFromCache(key: String): Any
  def findNode(key: String): Int
}