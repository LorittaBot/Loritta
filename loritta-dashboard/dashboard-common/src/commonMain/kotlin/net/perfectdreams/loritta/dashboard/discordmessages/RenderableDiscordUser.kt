package net.perfectdreams.loritta.dashboard.discordmessages

import kotlinx.serialization.Serializable

@Serializable
data class RenderableDiscordUser(
    val name: String,
    val avatarUrl: String,
    val bot: Boolean
) {
    /* companion object {
        fun fromDiscordUser(user: DiscordUser): RenderableDiscordUser {
            // TODO - htmx-mix: Refactor this!
            val avatarId = user.avatarId
            val url = if (avatarId != null) {
                "https://cdn.discordapp.com/avatars/${user.id}/${avatarId}.png"
            } else {
                "https://cdn.discordapp.com/embed/avatars/${(user.id shr 22) % 6}.png"
            }

            return RenderableDiscordUser(
                user.globalName ?: user.name,
                url,
                true
            )
        }
    } */
}