package net.perfectdreams.loritta.platform.discord.legacy.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import net.dv8tion.jda.api.entities.TextChannel
import net.perfectdreams.loritta.api.entities.Channel
import net.perfectdreams.loritta.api.entities.Member

open class DiscordChannel(@JsonIgnore val handle: net.dv8tion.jda.api.entities.MessageChannel) : Channel {
    override val name: String
        get() = handle.name
    override val participants: List<Member>
        get() = if (handle is TextChannel) handle.members.map { DiscordMember(it) } else listOf()
}