title: "Clusters and shards"
authors: [ "arth", "mrpowergamerbr" ]
---
Sharding is separating several instances of Discord, so that several instances can control different servers.

By default Discord makes that all bots that are in more than 2,500 servers need to use shards. Each shard can control, at most, 2,500 servers (if you have a bot and it doesn't do sharding, it cannot be added in more than 2,500 servers).

A shard would be as if you opened Discord again, only that each new "open Discord" possessed different servers.

Sharding is used to decrease the "weight" of a bot on Discord servers, and also is advantageous for bots, since each shard has a different rate limit, causing less problems related to this.

Clusters are a set of shards of Loritta. Each cluster is an application that runs several shards, thus in case it needs to update Loritta or in case there is some problem in the server, it will only affect those shards that are in the Cluster, instead of affecting all the servers that Loritta is in.

You can see all of them using the command `+ping clusters` and, if you are curious to know which is the cluster of your server, use `+ping` or `+serverinfo` to know!

It is not possible to choose which cluster or shard is that of your server, since each cluster has shards automatically allocated to it. And all clusters are equal, so the only reason for you to want to change cluster would be if you like the name of another cluster more.

## Things that Clusters influence on my functioning

* [Good Morning & Co.](/extras/faq-loritta/bomdiaecia).
* Commands and messages that involve emoji.
* Instabilities (I can become unstable in one server and be functioning normally in another due to the clusters system).
