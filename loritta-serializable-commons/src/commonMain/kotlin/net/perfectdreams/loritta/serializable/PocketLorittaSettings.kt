package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class PocketLorittaSettings(
    val lorittaCount: Int,
    val pantufaCount: Int,
    val gabrielaCount: Int
)