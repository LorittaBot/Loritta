package net.perfectdreams.loritta.morenitta.api.entities

import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.DiscordMessageChannel

interface Message {
	val author: User
	// val member: Member?
	val content: String
	val mentionedUsers: List<User>
	val channel: DiscordMessageChannel

	suspend fun delete()
}