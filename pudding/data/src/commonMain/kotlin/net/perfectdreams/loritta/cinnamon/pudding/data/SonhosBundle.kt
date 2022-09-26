package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class SonhosBundle(
    val id: Long,
    val active: Boolean,
    val price: Double,
    val sonhos: Long
)