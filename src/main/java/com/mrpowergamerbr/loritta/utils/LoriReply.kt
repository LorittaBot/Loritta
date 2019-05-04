package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.commands.CommandContext
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.api.entities.LorittaEmote
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext

class LoriReply(
		val message: String = " ",
		val prefix: String? = null,
		val forceMention: Boolean = false,
		val hasPadding: Boolean = true,
		val mentionUser: Boolean = true
) {
	constructor(message: String = " ",
				prefix: LorittaEmote,
				forceMention: Boolean = false,
				hasPadding: Boolean = true,
				mentionUser: Boolean = true
	) : this(message, prefix.asMention, forceMention, hasPadding, mentionUser)

	fun build(commandContext: CommandContext) = build(commandContext.userHandle.asMention, commandContext.getAsMention(true))

	fun build(commandContext: LorittaCommandContext): String {
		if (commandContext !is DiscordCommandContext)
			throw UnsupportedOperationException("I don't know how to handle a $commandContext yet!")
		return build(commandContext.userHandle.asMention, commandContext.getAsMention(true))
	}

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