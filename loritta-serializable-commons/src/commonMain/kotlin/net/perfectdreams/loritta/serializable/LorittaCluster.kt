package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class LorittaCluster(
    val id: Int,
    val name: String,
    val minShard: Int,
    val maxShard: Int,
    val websiteUrl: String,
    val rpcUrl: String
)