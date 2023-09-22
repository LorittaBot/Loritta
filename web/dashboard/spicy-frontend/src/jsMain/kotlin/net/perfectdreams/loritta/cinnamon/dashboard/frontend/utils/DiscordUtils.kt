package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.DiscordCdn
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.Image

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
}