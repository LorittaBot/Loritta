package net.perfectdreams.loritta.cinnamon.discord.utils.sources

import dev.kord.common.entity.DiscordUser
import net.perfectdreams.loritta.cinnamon.utils.Placeholders
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordUserAvatar

class UserTokenSource(private val user: DiscordUser) : TokenSource {
    override fun tokens() = mapOf(
        Placeholders.USER_MENTION to "<@${user.id}>",
        Placeholders.USER_NAME_SHORT to user.username,
        Placeholders.USER_NAME to user.username,
        Placeholders.USER_DISCRIMINATOR to user.discriminator,
        Placeholders.USER_ID to user.id.toString(),
        Placeholders.USER_AVATAR_URL to DiscordUserAvatar(user.id, user.discriminator, user.avatar).cdnUrl.toUrl(),
        Placeholders.USER_TAG to "${user.username}#${user.discriminator}",

        Placeholders.USER_DISCRIMINATOR to user.discriminator,
        Placeholders.Deprecated.USER_ID to user.id.toString(),
        Placeholders.Deprecated.USER_AVATAR_URL to DiscordUserAvatar(user.id, user.discriminator, user.avatar).cdnUrl.toUrl()
    )
}