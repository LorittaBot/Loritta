package net.perfectdreams.loritta.deviouscache.data

import kotlinx.serialization.Serializable

@Serializable
data class DeviousGatewaySession(
    val sessionId: String,
    val resumeGatewayUrl: String,
    var sequence: Int
)