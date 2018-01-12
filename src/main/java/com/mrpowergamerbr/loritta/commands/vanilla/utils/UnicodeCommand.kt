package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale


class UnicodeCommand : AbstractCommand("unicode", category = CommandCategory.UTILS) {
	override fun getUsage(): String {
		return "mensagem"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["UNICODE_Description"]
	}

	override fun getExample(): List<String> {
		return listOf("Loritta é muito fofa!");
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val message = context.rawArgs.joinToString(" ")

			val responses = mutableListOf(
					LoriReply(
							message.escapeMentions(),
							prefix = "\uD83D\uDC81"
					)
			)

			for (cp in message.codePoints()) {
				responses.add(
						LoriReply(
								"`${LorittaUtils.toUnicode(cp)}` ${Character.toChars(cp).joinToString("")} *${Character.getName(cp)}*",
								mentionUser = false
						)
				)
			}

			var reply = ""
			for (loriReply in responses) {
				val _message = loriReply.build(context) + "\n"

				if (reply.length + _message.length > 2000) {
					break
				}

				reply += _message
			}

			context.sendMessage(reply)
		} else {
			this.explain(context)
		}
	}
}