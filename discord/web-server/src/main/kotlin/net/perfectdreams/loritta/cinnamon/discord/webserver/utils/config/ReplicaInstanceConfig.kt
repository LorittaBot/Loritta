package net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class ReplicaInstanceConfig(
    val replicaId: Int,
    val minShard: Int,
    val maxShard: Int
)