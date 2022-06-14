package net.perfectdreams.loritta.cinnamon.dashboard.common.responses

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.ShipEffect

@Serializable
class GetShipEffectsResponse(
    val effects: List<ShipEffect>,
    val resolvedUsers: List<CachedUserInfo>
) : LorittaResponse()