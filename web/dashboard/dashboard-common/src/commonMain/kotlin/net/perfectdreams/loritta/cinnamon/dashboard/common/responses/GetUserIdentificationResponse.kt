package net.perfectdreams.loritta.cinnamon.dashboard.common.responses

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

@Serializable
data class GetUserIdentificationResponse(
    val id: UserId,
    val username: String,
    val discriminator: String,
    val avatarId: String?,
    val verified: Boolean,
    val money: Long,
    val displayAds: Boolean,
) : LorittaResponse()