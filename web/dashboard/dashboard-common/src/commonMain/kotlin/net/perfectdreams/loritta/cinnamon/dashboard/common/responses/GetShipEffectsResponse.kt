package net.perfectdreams.loritta.cinnamon.dashboard.common.responses

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.serializable.ShipEffect

@Serializable
class GetShipEffectsResponse(
    val effects: List<ShipEffect>,
    val resolvedUsers: List<CachedUserInfo>
) : LorittaResponse()