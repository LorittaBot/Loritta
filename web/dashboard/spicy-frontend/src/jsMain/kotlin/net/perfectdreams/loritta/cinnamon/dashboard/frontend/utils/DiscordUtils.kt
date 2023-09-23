package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.DiscordCdn
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.Image
import net.perfectdreams.loritta.serializable.DiscordChannel
import net.perfectdreams.loritta.serializable.NewsDiscordChannel
import net.perfectdreams.loritta.serializable.TextDiscordChannel

object DiscordUtils {
    fun getUserAvatarUrl(userId: Long, avatarHash: String?): String {
        val userIdULong = userId.toULong()
        return if (avatarHash != null) {
            DiscordCdn.userAvatar(userIdULong, avatarHash)
                .toUrl()
        } else {
            DiscordCdn.defaultAvatar(userIdULong)
                .toUrl {
                    format = Image.Format.PNG // For some weird reason, the default avatars aren't available in webp format (why?)
                }
        }
    }

    fun getIconForChannel(channel: DiscordChannel) = when (channel) {
        // is CategoryDiscordChannel -> TODO()
        // is ForumDiscordChannel -> TODO()
        is NewsDiscordChannel -> SVGIconManager.discordNewsChannel
        is TextDiscordChannel -> SVGIconManager.discordTextChannel
        // is StageDiscordChannel -> TODO()
        // is UnknownDiscordChannel -> TODO()
        // is VoiceDiscordChannel -> TODO()
        else -> SVGIconManager.discordTextChannel
    }
}