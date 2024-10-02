package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable

@Serializable
data class GetWorldInfoRequest(val worldName: String) : LoriTuberRequest()

@Serializable
data class GetWorldInfoResponse(
    val currentTick: Long,
    val lastUpdate: Long,
    val averageTickDuration: Double
) : LoriTuberResponse()