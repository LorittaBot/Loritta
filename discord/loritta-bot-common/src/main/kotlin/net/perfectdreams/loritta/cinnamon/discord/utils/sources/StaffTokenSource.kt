package net.perfectdreams.loritta.cinnamon.discord.utils.sources

import dev.kord.core.Kord
import dev.kord.core.cache.data.MemberData
import dev.kord.core.cache.data.UserData
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordUserAvatar
import net.perfectdreams.loritta.common.utils.Placeholders

class StaffTokenSource(
    private val kord: Kord,
    private val user: UserData,
    private val member: MemberData?
) : TokenSource {
    override fun tokens() = mapOf(
        Placeholders.STAFF_MENTION to "<@${user.id}>",
        Placeholders.STAFF_NAME_SHORT to user.username,
        Placeholders.STAFF_NAME to user.username,
        Placeholders.STAFF_DISCRIMINATOR to user.discriminator,
        Placeholders.STAFF_ID to user.id.toString(),
        Placeholders.STAFF_AVATAR_URL to DiscordUserAvatar(
            kord,
            user.id,
            user.discriminator,
            user.avatar
        ).cdnUrl.toUrl(),
        Placeholders.STAFF_TAG to "${user.username}#${user.discriminator}",

        Placeholders.STAFF_NICKNAME to (member?.nick?.value ?: user.username),

        Placeholders.Deprecated.STAFF_ID to user.id.toString(),
        Placeholders.Deprecated.STAFF_DISCRIMINATOR to user.discriminator,
        Placeholders.Deprecated.STAFF_AVATAR_URL to DiscordUserAvatar(
            kord,
            user.id,
            user.discriminator,
            user.avatar
        ).cdnUrl.toUrl()
    )
}