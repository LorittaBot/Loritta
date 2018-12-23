package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.loritta.api.commands.LorittaCommandContext

class LoriReply(
		val message: String = " ",
		val prefix: String? = null,
		val forceMention: Boolean = false,
		val hasPadding: Boolean = true,
		val mentionUser: Boolean = true
) {
	fun build(commandContext: CommandContext) = build(commandContext.userHandle.asMention, commandContext.getAsMention(true))

	fun build(commandContext: LorittaCommandContext) = build(commandContext.userHandle.asMention, commandContext.getAsMention(true))

	fun build(user: User) = build(user.asMention, null)

	fun build() = build(null, null)

	fun build(mention: String? = null, contextAsMention: String? = null): String {
		var send = ""
		if (prefix != null) {
			send = "$prefix **|** "
		} else if (hasPadding) {
			send = Constants.LEFT_PADDING + " **|** "
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
}