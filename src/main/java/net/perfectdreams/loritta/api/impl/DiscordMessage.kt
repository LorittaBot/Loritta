package net.perfectdreams.loritta.api.impl

import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User

class DiscordMessage(val handle: net.dv8tion.jda.core.entities.Message) : Message {
	override val author: User
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override val content = handle.contentRaw
}