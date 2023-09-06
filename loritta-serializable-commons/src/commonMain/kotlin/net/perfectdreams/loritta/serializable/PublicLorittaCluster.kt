package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

/**
 * Same thing as [LorittaCluster], but without private information about the cluster (like [LorittaCluster.rpcUrl])
 */
@Serializable
data class PublicLorittaCluster(
    val id: Int,
    val name: String,
    val minShard: Int,
    val maxShard: Int,
    val websiteUrl: String
)