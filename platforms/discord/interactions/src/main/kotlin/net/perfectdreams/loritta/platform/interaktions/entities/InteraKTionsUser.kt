package net.perfectdreams.loritta.platform.interaktions.entities

import net.perfectdreams.discordinteraktions.platforms.kord.entities.KordUser
import net.perfectdreams.loritta.discord.DiscordUserAvatar
import net.perfectdreams.loritta.discord.entities.DiscordUser

class InteraKTionsUser(val user: net.perfectdreams.discordinteraktions.api.entities.User) : DiscordUser {
    override val id: ULong
        get() = user.id.value
    override val name by user::name

    override val avatar = DiscordUserAvatar(
        user.id.value,
        user.discriminator.toInt(),
        user.avatar.avatarId
    )

    override val asMention: String
        get() = "<@${id}>"


    override val banner: String?
        get() = (user as KordUser).handle.banner // TODO: Add to Discord InteraKTions
}