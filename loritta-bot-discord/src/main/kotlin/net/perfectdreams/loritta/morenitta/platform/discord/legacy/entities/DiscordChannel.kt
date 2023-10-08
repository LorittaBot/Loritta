package net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.perfectdreams.loritta.common.entities.Channel
import net.perfectdreams.loritta.common.entities.Member

open class DiscordChannel(@JsonIgnore val handle: net.dv8tion.jda.api.entities.channel.middleman.MessageChannel) : Channel {
    override val name: String
        get() = handle.name
    override val participants: List<Member>
        get() = if (handle is TextChannel) handle.members.map { DiscordMember(it) } else listOf()
}