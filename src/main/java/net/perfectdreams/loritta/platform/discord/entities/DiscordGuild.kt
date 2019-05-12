package net.perfectdreams.loritta.platform.discord.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import net.perfectdreams.loritta.api.entities.Guild
import net.perfectdreams.loritta.api.entities.Member
import net.perfectdreams.loritta.api.entities.MessageChannel

class DiscordGuild(@JsonIgnore val handle: net.dv8tion.jda.api.entities.Guild) : Guild {
	override val id: String
		get() = handle.id
	override val name: String
		get() = handle.name
	override val iconUrl: String?
		get() = handle.iconUrl
	override val members: List<Member>
		get() = handle.members.map { DiscordMember(it) }
	override val messageChannels: List<MessageChannel>
		get() = handle.textChannels.map { DiscordMessageChannel(it) }
}