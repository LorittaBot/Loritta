package net.perfectdreams.loritta.platform.discord.entities

import net.perfectdreams.loritta.api.entities.Guild
import net.perfectdreams.loritta.api.entities.Member

class DiscordGuild(val handle: net.dv8tion.jda.core.entities.Guild) : Guild {
	override val members: List<Member>
		get() = handle.members.map { DiscordMember(it) }
}