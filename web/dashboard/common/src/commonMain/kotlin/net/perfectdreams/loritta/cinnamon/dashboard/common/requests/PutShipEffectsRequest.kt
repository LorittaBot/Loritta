package net.perfectdreams.loritta.cinnamon.dashboard.common.requests

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.dashboard.common.ShipPercentage

@Serializable
data class PutShipEffectsRequest(
    val receivingEffectUserId: Long,
    val percentage: ShipPercentage
)