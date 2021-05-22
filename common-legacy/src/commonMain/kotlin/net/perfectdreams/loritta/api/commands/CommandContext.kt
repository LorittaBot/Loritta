package net.perfectdreams.loritta.api.commands

import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.entities.LorittaEmote
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.api.messages.LorittaMessage
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.image.Image
import net.perfectdreams.loritta.utils.Emotes

abstract class CommandContext(
		// Nifty trick: By keeping it "open", implementations can override this variable.
		// By doing this, classes can use their own platform implementation (example: LorittaDiscord instead of LorittaBot)
		// If you don't keep it "open", the type will always be "LorittaBot", which sucks.
		open val loritta: LorittaBot,
		val command: Command<CommandContext>,
		val args: List<String>,
		val message: Message,
		val locale: BaseLocale
) {
	val sender = message.author

	open suspend fun sendMessage(content: String) = message.channel.sendMessage(content)
	open suspend fun sendMessage(lorittaMessage: LorittaMessage) = message.channel.sendMessage(lorittaMessage)
	open suspend fun sendImage(image: Image, fileName: String = "image.png", content: String = getUserMention(true)) = message.channel.sendFile(image.toByteArray(), fileName, content)
	open suspend fun sendFile(byteArray: ByteArray, fileName: String, content: String = getUserMention(true)) = message.channel.sendFile(byteArray, fileName, content)

	abstract suspend fun user(argument: Int): User?
	suspend fun userOrFail(argument: Int) = validate(user(argument), argument)
	abstract suspend fun imageUrl(argument: Int, searchPreviousMessages: Int = 25): String?
	abstract suspend fun image(argument: Int, searchPreviousMessages: Int = 25, createTextAsImageIfNotFound: Boolean = true): Image?
	suspend fun imageOrFail(argument: Int) = validate(image(argument))

	suspend fun reply(vararg replies: LorittaReply) = reply(replies.toList())
	suspend fun reply(replies: List<LorittaReply>): Message {
		val message = StringBuilder()
		for (loriReply in replies) {
			message.append(loriReply.build(this))
			message.append("\n")
		}
		return sendMessage(message.toString())
	}

	/**
	 * Creates a [LorittaReply] with the specified parameters and sends the message.
	 *
	 * This is a helper method for [reply]
	 *
	 * @see    reply
	 * @return the sent message
	 */
	suspend fun reply(
			message: String = " ",
			prefix: String? = null,
			forceMention: Boolean = false,
			hasPadding: Boolean = true,
			mentionUser: Boolean = true
	) = reply(
			LorittaReply(
					message,
					prefix,
					forceMention,
					hasPadding,
					mentionUser
			)
	)

	/**
	 * Creates a [LorittaReply] with the specified parameters and sends the message.
	 *
	 * This is a helper method for [reply]
	 *
	 * @see    reply
	 * @return the sent message
	 */
	suspend fun reply(
			message: String = " ",
			prefix: LorittaEmote,
			forceMention: Boolean = false,
			hasPadding: Boolean = true,
			mentionUser: Boolean = true
	) = reply(
			LorittaReply(
					message,
					prefix,
					forceMention,
					hasPadding,
					mentionUser
			)
	)

	/**
	 * Throws a [CommandException], halting command execution
	 *
	 * @param message the message that will be sent
	 * @oaram prefix  the message's prefix as a emote (see [LorittaReply.prefix])
	 * @see fail
	 * @see CommandException
	 */
	fun fail(message: String, prefix: LorittaEmote): Nothing = throw CommandException(message, prefix.toString())

	/**
	 * Throws a [CommandException], halting command execution
	 *
	 * @param message the message that will be sent
	 * @oaram prefix  the message's prefix (see [LorittaReply.prefix])
	 * @see fail
	 * @see CommandException
	 */
	fun fail(message: String, prefix: String? = null): Nothing = throw CommandException(message, prefix ?: Emotes.LORI_CRYING.toString())

	/**
	 * Throws a [CommandException], halting command execution
	 *
	 * @param reply the message that will be sent
	 * @see fail
	 * @see CommandException
	 */
	fun fail(reply: LorittaReply): Nothing = throw CommandException(reply)

	fun getUserMention(addSpace: Boolean): String {
		return message.author.asMention + (if (addSpace) " " else "")
	}

	inline fun <reified T> checkType(source: CommandContext): T {
		if (source !is T)
			fail(locale["commands.commandNotSupportedInThisPlatform"], Emotes.LORI_CRYING.toString())

		return source
	}

	suspend fun validate(image: Image?): Image {
		if (image == null) {
			if (args.isEmpty())
				explainAndExit()
			else
				fail(locale["commands.noValidImageFound", Emotes.LORI_CRYING], Emotes.LORI_CRYING.toString())
		}

		return image
	}

	suspend fun validate(user: User?, argumentIndex: Int = 0): User {
		if (user == null) {
			if (args.isEmpty())
				explainAndExit()
			else
				fail(locale["commands.userDoesNotExist", "${args.getOrNull(argumentIndex)?.replace("`", "")}"], Emotes.LORI_CRYING.toString())
		}

		return user
	}

	/**
	 * Sends the command help to the current channel
	 */
	abstract suspend fun explain()

	/**
	 * Sends the command help to the current channel and halts the command flow
	 *
	 * @see explain
	 */
	suspend fun explainAndExit(): Nothing {
		explain()
		throw SilentCommandException()
	}
}