# DistCache
This is an implementation of a distributed key-value cache on top of Akka. For more info on Akka please refer to that link: https://akka.io/

# Instructions
To run the cache, you need to have the `sbt` tool in you machine. Refer to the following link for the official instructions: https://www.scala-sbt.org/release/docs/Setup.html

# Clone the project
Run `git clone https://github.com/gsvic/DistCache.git`

## Run the master node
To run the master, run the following command
`sbin/run_master.sh 2551`

Note that `2551` is the port number, which can be changed as well.

## Run a slave node
To run a slave, run the following command
`sbin/run_slave.sh 2552`

You can run multiple nodes on the same or different machines, by running ``sbin/run_slave.sh {PORT_NUMBER}` multiple times with different port numbers. 


## Perform operations
After running the master node, you will able to see a CLI running for master which can be used for executing the following operations:
-ls: Lists all elements in each node
-nodes: Lists all nodes joined in the cluster
-put: Puts a key-value pair into cache
-evic: Evicts a key from cache
