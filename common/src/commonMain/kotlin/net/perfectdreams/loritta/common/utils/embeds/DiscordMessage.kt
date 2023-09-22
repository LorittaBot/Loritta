package net.perfectdreams.loritta.common.utils.embeds

import kotlinx.serialization.Serializable

@Serializable
data class DiscordMessage(
    val content: String,
    val embed: DiscordEmbed? = null,
    val components: List<DiscordComponent>? = null
)