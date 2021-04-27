package net.perfectdreams.loritta.platform.interaktions.entities

import net.perfectdreams.loritta.common.pudding.entities.User
import net.perfectdreams.loritta.discord.DiscordUserAvatar

class InteraKTionsUser(val user: net.perfectdreams.discordinteraktions.api.entities.User) : User {
    override val id: Long
        get() = user.id.value
    override val name by user::name

    override val avatar = DiscordUserAvatar(
        user.id.value,
        user.discriminator.toInt(),
        user.avatar.avatarId
    )

    override val asMention: String
        get() = "<@${id}>"
}