package net.perfectdreams.loritta.api.impl

import net.perfectdreams.loritta.api.entities.Message

class DiscordMessage(val handle: net.dv8tion.jda.core.entities.Message) : Message {
	override val author = DiscordUser(handle.author)
	override val content = handle.contentRaw
}