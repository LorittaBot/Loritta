package net.perfectdreams.loritta.dashboard.discordmessages

import kotlinx.serialization.Serializable

@Serializable
data class DiscordMessage(
    val content: String,
    val tts: Boolean = false,
    val embeds: List<DiscordEmbed>? = null,
    val components: List<DiscordComponent>? = null
)