package net.perfectdreams.loritta.platform.discord.entities

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.MessageInteractionFunctions
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser

class DiscordMessage(val handle: net.dv8tion.jda.api.entities.Message) : Message {
	override val author = JDAUser(handle.author)
	override val content = handle.contentRaw
	override val mentionedUsers: List<User>
		get() = handle.mentionedUsers.map { JDAUser(it) }

	override suspend fun delete() {
		handle.delete().await()
	}

	/**
	 * When the command executor sends a message on the same text channel as the executed command
	 *
	 * @param context  the context of the message
	 * @param function the callback that should be invoked
	 * @return         the message object for chaining
	 */
	fun onResponseByAuthor(context: LorittaCommandContext, function: suspend (LorittaMessageEvent) -> Unit): DiscordMessage {
		if (context !is DiscordCommandContext)
			throw UnsupportedOperationException("I don't know how to handle a $context yet!")

		val functions = loritta.messageInteractionCache.getOrPut(this.handle.idLong) { MessageInteractionFunctions(this.handle.guild?.idLong, this.handle.channel?.idLong, context.userHandle.id) }
		functions.onResponseByAuthor = function
		return this
	}

	fun invalidateInteraction() {
		loritta.messageInteractionCache.remove(this.handle.idLong)
	}
}