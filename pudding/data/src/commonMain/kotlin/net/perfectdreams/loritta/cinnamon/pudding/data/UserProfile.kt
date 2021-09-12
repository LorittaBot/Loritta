package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: UserId,
    val money: Long
)