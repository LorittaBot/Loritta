package net.perfectdreams.loritta.platform.discord.entities

import net.dv8tion.jda.core.entities.TextChannel
import net.perfectdreams.loritta.api.entities.Channel
import net.perfectdreams.loritta.api.entities.Member

open class DiscordChannel(val handle: net.dv8tion.jda.core.entities.MessageChannel) : Channel {
    override val name: String
        get() = handle.name
    override val participants: List<Member>
        get() = if (handle is TextChannel) handle.members.map { DiscordMember(it) } else listOf()
}