package net.perfectdreams.loritta.platform.discord.legacy.entities

import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.platform.discord.legacy.entities.jda.JDAUser

class DiscordMessage(val handle: net.dv8tion.jda.api.entities.Message) : Message {
	override val author = JDAUser(handle.author)
	override val content = handle.contentRaw
	override val mentionedUsers: List<User>
		get() = handle.mentionedUsers.map { JDAUser(it) }
	override val channel = DiscordMessageChannel(handle.channel)

	override suspend fun delete() {
		handle.delete().await()
	}
}