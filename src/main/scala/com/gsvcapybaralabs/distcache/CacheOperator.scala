package com.gsvcapybaralabs.distcache

import akka.actor.ActorRef

class CacheOperator()

case class Put(key: String, value: Any) extends CacheOperator
case class Hit(key: String) extends CacheOperator
case class Evict(key: String) extends CacheOperator
final case class Request(key: String, replyTo: ActorRef)
final case class Cached(key: String, value: Option[Any])
