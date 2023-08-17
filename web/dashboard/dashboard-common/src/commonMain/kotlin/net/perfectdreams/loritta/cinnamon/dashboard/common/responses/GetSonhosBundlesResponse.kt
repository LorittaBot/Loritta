package net.perfectdreams.loritta.cinnamon.dashboard.common.responses

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.serializable.SonhosBundle

@Serializable
data class GetSonhosBundlesResponse(
    val bundles: List<SonhosBundle>
) : LorittaResponse()