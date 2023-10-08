package net.perfectdreams.loritta.morenitta.utils.devious

import kotlinx.serialization.Serializable

@Serializable
data class GatewaySessionData(
    val sessionId: String,
    val resumeGatewayUrl: String,
    val sequence: Long,
    val guilds: List<Long>
)