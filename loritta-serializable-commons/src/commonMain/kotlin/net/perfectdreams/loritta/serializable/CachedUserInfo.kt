package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
class CachedUserInfo(
    val id: UserId,
    val name: String,
    val discriminator: String,
    val globalName: String?,
    val avatarId: String?
)