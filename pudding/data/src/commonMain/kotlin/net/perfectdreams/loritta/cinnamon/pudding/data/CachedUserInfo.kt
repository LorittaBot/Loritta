package net.perfectdreams.loritta.cinnamon.pudding.data

import kotlinx.serialization.Serializable

@Serializable
class CachedUserInfo(
    val id: UserId,
    val name: String,
    val discriminator: String,
    val avatarId: String?
)