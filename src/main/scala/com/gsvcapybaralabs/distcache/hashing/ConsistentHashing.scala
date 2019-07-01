package com.gsvcapybaralabs.distcache.hashing

import java.util

import com.gsvcapybaralabs.distcache.CacheNode

import scala.collection.mutable
import scala.util.Random

/***
  * The consistent hashing implementation
  */
class ConsistentHashing(){
  val REPLICATION: Int = 3
  val nodeRing: NodeRing = new NodeRing()

  def findNode(key: String): Int = nodeRing.findNode(key)
  def addNode(id: Int): Unit = nodeRing.addNode(id)
}

/***
  * Represents a ring in which the nodes and the keys are mapped in a circular fashion
  */
case class NodeRing(){
  val MAX_NUM: Double = 5
  val NUM_LOCATIONS: Int = 10

  var nodes = new mutable.TreeMap[Double, CacheNode]()
  val reservedLocations = mutable.Set[Double]()

  /**
    * Adds a new node to the node ring
    * @param id
    */
  def addNode(id: Int): Unit = {
    val node = CacheNode(id)
    val locations = findLocs()

    locations.foreach(loc => nodes.put(loc, node))
  }

  /**
    * Removes a node from the node ring
    * @param id
    */
  def removeNode(id: Int): Unit = {
    nodes.filter(x => x._2.id == id).foreach{ n =>
      reservedLocations.remove(n._1)
      nodes.remove(n._1)
    }
  }

  /**
    * Finds NUM_LOCATIONS random locations in the node ring
    * @return Returns the list with the locations
    */
  def findLocs(): List[Double] = {
    List.range(0, NUM_LOCATIONS).map{ loc =>
      var loc = (Random.nextDouble()*MAX_NUM) % MAX_NUM
      while (reservedLocations.contains(loc)){ loc = (Random.nextDouble() * MAX_NUM) % MAX_NUM }
      reservedLocations.add(loc)
      loc
    }
  }

  /**
    * Searches for the nearest node in the node ring using binary search
    * @param point
    * @return The nearest node
    */
  def searchNode(point: Double): Double = {
    var nodePoints = nodes.keys.toList

    var pivot = nodePoints.size / 2
    while (pivot > 0) {
      val slice1 = nodePoints.slice(0, pivot)
      val slice2 = nodePoints.slice(pivot + 1, nodePoints.size)

      if (point > nodePoints(pivot) && slice2.size > 0)
        nodePoints = slice2
      else
        nodePoints = slice1

      pivot = nodePoints.size / 2
    }

    val p = nodePoints(pivot)

    if (p >= point)
      getPrevious(p)._1

    p
  }

  /**
    * Given a double point returns the nearest node to that point
    * @param point
    * @return
    */
  def findNode(point: Double): Int = {
    nodes.get(searchNode(point)).get.id
  }

  /**
    * Given a hash key returns the nearest node to that key based on its hashCode
    * @param point
    * @return
    */
  def findNode(key: String): Int = {
    findNode(key.hashCode() % MAX_NUM)
  }

  def getNeighbors(id: Int): Set[CacheNode] = {
    val locations = nodes.filter(_._2.id == id)
    val neighbors = locations.map(x => getPrevious(x._1)).map(x => nodes.get(x._1).get).filter(_.id != id).toSet

    neighbors
  }

  def getNodeLocations(id: Int): List[Double] = {
    nodes.filter(_._2.id == id).keys.toList
  }

  def getNext(key: Double): (Double, CacheNode) = {
    if (!nodes.contains(key))
      throw new Exception(s"Node with key $key did not found")

    if (key == nodes.lastKey)
      return nodes.toList(0)

    val idx = util.Arrays.binarySearch(nodes.keys.toArray, key) + 1
    nodes.toList(idx)
  }

  def getPrevious(key: Double): (Double, CacheNode) = {
    if (!nodes.contains(key))
      throw new Exception(s"Node with key $key did not found")

    if (key == nodes.firstKey)
      return nodes.last

    val idx = util.Arrays.binarySearch(nodes.keys.toArray, key) - 1
    nodes.toList(idx)
  }


}