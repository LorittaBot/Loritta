package net.perfectdreams.loritta.morenitta.utils.gamersafer

import kotlinx.serialization.Serializable

@Serializable
data class GamerSaferGuildInviteAdditionalData(
    val userId: Long,
    val token: String
)

@Serializable
data class GamerSaferPlayerVerificationAdditionalData(
    val guildId: Long,
    val userId: Long,
    val token: String
)