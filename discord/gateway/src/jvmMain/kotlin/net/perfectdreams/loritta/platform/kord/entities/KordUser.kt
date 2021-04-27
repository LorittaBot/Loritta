package net.perfectdreams.loritta.platform.kord.entities

import net.perfectdreams.loritta.common.pudding.entities.User
import net.perfectdreams.loritta.common.pudding.entities.UserAvatar
import net.perfectdreams.loritta.discord.DiscordUserAvatar

class KordUser(private val handle: dev.kord.core.entity.User) : User {
    override val id: Long
        get() = handle.id.value
    override val asMention: String
        get() = handle.mention
    override val name: String
        get() = handle.username
    override val avatar: UserAvatar
        get() = DiscordUserAvatar(handle.id.value, handle.discriminator.toInt(), handle.avatar.data.avatar)
}