package net.perfectdreams.loritta.cinnamon.discord.webserver.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class ReplicasConfig(
    val getReplicaIdFromHostname: Boolean,
    val replicaIdOverride: Int? = null
)