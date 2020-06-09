package net.perfectdreams.loritta.utils

data class ClusterOfflineException(val id: Long, val name: String) : RuntimeException("Shard $id ($name) is offline")