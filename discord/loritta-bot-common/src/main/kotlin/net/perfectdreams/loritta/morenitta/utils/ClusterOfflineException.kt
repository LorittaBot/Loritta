package net.perfectdreams.loritta.morenitta.utils

data class ClusterOfflineException(val id: Long, val name: String) : RuntimeException("Cluster $id ($name) is offline")