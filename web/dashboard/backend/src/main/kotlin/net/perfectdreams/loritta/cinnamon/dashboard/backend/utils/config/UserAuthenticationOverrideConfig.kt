package net.perfectdreams.loritta.cinnamon.dashboard.backend.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class UserAuthenticationOverrideConfig(
    val enabled: Boolean,
    val id: Long,
    val name: String,
    val discriminator: String,
    val globalName: String?,
    val avatarId: String?
)