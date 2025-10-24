package net.perfectdreams.loritta.dashboard.discordmessages

import kotlinx.serialization.Serializable

@Serializable
data class RenderableDiscordUser(
    val name: String,
    val avatarUrl: String,
    val bot: Boolean,
    val isAppVerified: Boolean
)