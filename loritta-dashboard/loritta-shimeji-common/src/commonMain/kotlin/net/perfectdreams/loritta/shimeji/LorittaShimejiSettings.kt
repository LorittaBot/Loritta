package net.perfectdreams.loritta.shimeji

import kotlinx.serialization.Serializable

@Serializable
data class LorittaShimejiSettings(
    val lorittaCount: Int,
    val pantufaCount: Int,
    val gabrielaCount: Int,
    val activityLevel: ActivityLevel
)