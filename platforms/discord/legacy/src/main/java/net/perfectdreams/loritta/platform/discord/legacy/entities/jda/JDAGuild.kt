package net.perfectdreams.loritta.platform.discord.legacy.entities.jda

import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.api.entities.Member
import net.perfectdreams.loritta.api.entities.MessageChannel
import net.perfectdreams.loritta.platform.discord.legacy.entities.DiscordGuild
import net.perfectdreams.loritta.platform.discord.legacy.entities.DiscordMember
import net.perfectdreams.loritta.platform.discord.legacy.entities.DiscordMessageChannel

class JDAGuild(val handle: Guild) : DiscordGuild {
    override val id: Long
        get() = handle.idLong
    override val name: String
        get() = handle.name
    override val icon: String?
        get() = handle.iconId
    override val members: List<Member>
        get() = handle.members.map { DiscordMember(it) }
    override val messageChannels: List<MessageChannel>
        get() = handle.textChannels.map { DiscordMessageChannel(it) }
}