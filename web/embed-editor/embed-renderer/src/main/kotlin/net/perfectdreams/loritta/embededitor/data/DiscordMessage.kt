package net.perfectdreams.loritta.embededitor.data

import kotlinx.serialization.Serializable

@Serializable
data class DiscordMessage(
        val content: String,
        val embed: DiscordEmbed? = null
)