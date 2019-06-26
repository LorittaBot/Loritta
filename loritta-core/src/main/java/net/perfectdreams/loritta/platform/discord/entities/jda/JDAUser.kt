package net.perfectdreams.loritta.platform.discord.entities.jda

import com.fasterxml.jackson.annotation.JsonIgnore
import net.perfectdreams.loritta.platform.discord.entities.DiscordUser

open class JDAUser(@JsonIgnore val handle: net.dv8tion.jda.api.entities.User) : DiscordUser {
    override val id: Long
        get() = handle.idLong

    override val name: String
        get() = handle.name

    override val avatar: String?
        get() = handle.avatarId

    override val avatarUrl: String?
        get() = handle.avatarUrl

    override val effectiveAvatarUrl: String
        get() = handle.effectiveAvatarUrl

    override val defaultAvatarUrl: String
        get() = handle.defaultAvatarUrl

    override val asMention: String
        get() = handle.asMention

    override val isBot: Boolean
        get() = handle.isBot

    override val discriminator: String
        get() = handle.discriminator
}