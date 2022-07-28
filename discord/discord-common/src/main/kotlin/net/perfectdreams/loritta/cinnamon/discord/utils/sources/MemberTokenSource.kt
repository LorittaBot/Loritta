package net.perfectdreams.loritta.cinnamon.discord.utils.sources

import dev.kord.common.entity.DiscordGuildMember
import dev.kord.common.entity.DiscordUser
import net.perfectdreams.loritta.cinnamon.utils.Placeholders

class MemberTokenSource(private val user: DiscordUser, private val member: DiscordGuildMember) : TokenSource {
    override fun tokens() = inheritFromAndPutAll(
        UserTokenSource(user),
        Placeholders.USER_NICKNAME to (member.nick.value ?: user.username),
        Placeholders.Deprecated.USER_NICKNAME to (member.nick.value ?: user.username)
    )
}