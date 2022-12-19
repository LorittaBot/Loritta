package net.perfectdreams.loritta.serializable.lorituber

import kotlinx.serialization.Serializable

@Serializable
data class LoriTuberChannel(
    val id: Long,
    val name: String
)