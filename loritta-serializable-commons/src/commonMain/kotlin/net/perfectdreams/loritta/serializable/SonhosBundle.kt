package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class SonhosBundle(
    val id: Long,
    val active: Boolean,
    val price: Double,
    val sonhos: Long,
    val bonus: Long?
)