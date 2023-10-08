package net.perfectdreams.loritta.morenitta.utils

data class ClusterNotReadyException(val id: Long, val name: String) : RuntimeException("Cluster $id ($name) is not ready")