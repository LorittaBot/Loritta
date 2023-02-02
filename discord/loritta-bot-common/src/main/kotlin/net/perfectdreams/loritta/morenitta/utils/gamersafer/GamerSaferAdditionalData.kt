package net.perfectdreams.loritta.morenitta.utils.gamersafer

import kotlinx.serialization.Serializable

@Serializable
data class GamerSaferAdditionalData(
    val userId: Long,
    val token: String
)