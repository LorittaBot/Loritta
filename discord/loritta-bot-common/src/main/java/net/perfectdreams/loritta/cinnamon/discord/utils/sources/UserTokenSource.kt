package net.perfectdreams.loritta.cinnamon.discord.utils.sources

import dev.kord.core.Kord
import dev.kord.core.cache.data.MemberData
import dev.kord.core.cache.data.UserData
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordUserAvatar
import net.perfectdreams.loritta.cinnamon.utils.Placeholders

class UserTokenSource(
    private val kord: Kord,
    private val user: UserData,
    private val member: MemberData?
) : TokenSource {
    override fun tokens() = mapOf(
        Placeholders.USER_MENTION to "<@${user.id}>",
        Placeholders.USER_NAME_SHORT to user.username,
        Placeholders.USER_NAME to user.username,
        Placeholders.USER_DISCRIMINATOR to user.discriminator,
        Placeholders.USER_ID to user.id.toString(),
        Placeholders.USER_AVATAR_URL to DiscordUserAvatar(kord, user.id, user.discriminator, user.avatar).cdnUrl.toUrl(),
        Placeholders.USER_TAG to "${user.username}#${user.discriminator}",

        Placeholders.USER_NICKNAME to (member?.nick?.value ?: user.username),
        Placeholders.Deprecated.USER_NICKNAME to (member?.nick?.value ?: user.username),

        Placeholders.Deprecated.USER_ID to user.id.toString(),
        Placeholders.Deprecated.USER_DISCRIMINATOR to user.discriminator,
        Placeholders.Deprecated.USER_AVATAR_URL to DiscordUserAvatar(kord, user.id, user.discriminator, user.avatar).cdnUrl.toUrl()
    )
}