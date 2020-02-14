package net.perfectdreams.loritta.api.commands

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.api.messages.LorittaMessage
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.image.Image
import net.perfectdreams.loritta.utils.Emotes

abstract class CommandContext(
		val loritta: LorittaBot,
		val command: Command<CommandContext>,
		val args: List<String>,
		val message: Message,
		val locale: BaseLocale
) {
	val sender = message.author

	suspend fun sendMessage(content: String) = message.channel.sendMessage(content)
	suspend fun sendMessage(lorittaMessage: LorittaMessage) = message.channel.sendMessage(lorittaMessage)
	suspend fun sendImage(image: Image, fileName: String = "image.png", content: String = getUserMention(true)) = message.channel.sendFile(image.toByteArray(), fileName, content)

	abstract suspend fun user(argument: Int): User?
	abstract suspend fun imageUrl(argument: Int, searchPreviousMessages: Int = 0): String?
	abstract suspend fun image(argument: Int, searchPreviousMessages: Int = 0, createTextAsImageIfNotFound: Boolean = true): Image?

	suspend fun reply(vararg replies: LorittaReply) = reply(replies.toList())
	suspend fun reply(replies: List<LorittaReply>): Message {
		val message = StringBuilder()
		for (loriReply in replies) {
			message.append(loriReply.build(this))
			message.append("\n")
		}
		return sendMessage(message.toString())
	}

	fun getUserMention(addSpace: Boolean): String {
		return message.author.asMention + (if (addSpace) " " else "")
	}

	inline fun <reified T> checkType(source: CommandContext): T {
		if (source !is T)
			throw CommandException(locale["commands.commandNotSupportedInThisPlatform"], Emotes.LORI_CRYING.toString())

		return source
	}

	suspend fun validate(image: Image?): Image {
		if (image == null) {
			if (args.isEmpty()) {
				explain()
				throw SilentCommandException()
			} else {
				throw CommandException(locale["commands.noValidImageFound", Emotes.LORI_CRYING], Emotes.LORI_CRYING.toString())
			}
		}

		return image
	}

	abstract suspend fun explain()
}