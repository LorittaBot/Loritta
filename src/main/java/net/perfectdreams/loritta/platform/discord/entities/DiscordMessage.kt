package net.perfectdreams.loritta.platform.discord.entities

import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User

class DiscordMessage(val handle: net.dv8tion.jda.core.entities.Message) : Message {
	override val author = DiscordUser(handle.author)
	override val content = handle.contentRaw
	override val mentionedUsers: List<User>
		get() = handle.mentionedUsers.map { DiscordUser(it) }
}