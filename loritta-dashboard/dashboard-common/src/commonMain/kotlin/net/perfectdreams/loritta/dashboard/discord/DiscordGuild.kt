package net.perfectdreams.loritta.dashboard.discord

import kotlinx.serialization.Serializable

@Serializable
data class DiscordGuild(
    val id: Long,
    val name: String,
    val iconId: String?,
    val roles: List<DiscordRole>,
    val channels: List<DiscordChannel>,
    val emojis: List<DiscordEmoji>
) {
    fun getIconUrl(size: Int): String? {
        if (iconId == null)
            return null

        val ext = if (iconId.startsWith("a_"))
            "gif"
        else
            "png"
        return "https://cdn.discordapp.com/icons/$id/$iconId.$ext?size=$size"
    }
}