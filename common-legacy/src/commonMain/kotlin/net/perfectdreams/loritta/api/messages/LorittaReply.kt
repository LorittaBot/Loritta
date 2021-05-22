package net.perfectdreams.loritta.api.messages

import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.entities.LorittaEmote
import net.perfectdreams.loritta.api.entities.User

class LorittaReply(
		val message: String = " ",
		val prefix: String? = null,
		val forceMention: Boolean = false,
		val hasPadding: Boolean = true,
		val mentionUser: Boolean = true
) {
	constructor(message: String, prefix: LorittaEmote, forceMention: Boolean = false, hasPadding: Boolean = true, mentionUser: Boolean = true) :
			this(message, prefix.toString(), forceMention, hasPadding, mentionUser)

	fun build(commandContext: CommandContext): String {
		return build(commandContext.getUserMention(false), commandContext.getUserMention(true))
	}

	fun build(user: User) = build(user.asMention, null)

	fun build() = build(null, null)

	fun build(mention: String? = null, contextAsMention: String? = null): String {
		var send = ""
		if (prefix != null) {
			send = "$prefix **|** "
		} else if (hasPadding) {
			send = "$LEFT_PADDING **|** "
		}
		if (mentionUser && mention != null) {
			send = if (forceMention || contextAsMention == null) {
				"$send$mention "
			} else {
				send + contextAsMention
			}
		}
		send += message
		return send
	}

	companion object {
		private const val LEFT_PADDING = "\uD83D\uDD39"
	}
}