package net.perfectdreams.loritta.cinnamon.dashboard.common.embeds

import kotlinx.serialization.Serializable

@Serializable
data class DiscordMessage(
    val content: String,
    val embed: DiscordEmbed? = null
)