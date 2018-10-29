package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.entities.User

class LoriReply(
		val message: String = " ",
		val prefix: String? = null,
		val forceMention: Boolean = false,
		val hasPadding: Boolean = true,
		val mentionUser: Boolean = true
) {
	fun build(commandContext: CommandContext): String {
		var send = ""
		if (prefix != null) {
			send = prefix + " **|** "
		} else if (hasPadding) {
			send = Constants.LEFT_PADDING + " **|** "
		}
		if (mentionUser) {
			send = if (forceMention) {
				send + commandContext.userHandle.asMention + " "
			} else {
				send + commandContext.getAsMention(true)
			}
		}
		send += message
		return send
	}

	fun build(user: User): String {
		var send = ""
		if (prefix != null) {
			send = prefix + " **|** "
		} else if (hasPadding) {
			send = Constants.LEFT_PADDING + " **|** "
		}
		if (mentionUser) {
			send = send + user.asMention + " "
		}
		send += message
		return send
	}

	fun build(): String {
		var send = ""
		if (prefix != null) {
			send = prefix + " **|** "
		} else if (hasPadding) {
			send = Constants.LEFT_PADDING + " **|** "
		}
		send += message
		return send
	}
}