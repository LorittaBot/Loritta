package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.core.entities.User

class KissCommand : ActionCommand("kiss", listOf("beijar", "beijo")) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.format { commands.actions.kiss.description }
	}

	override fun getResponse(locale: LegacyBaseLocale, first: User, second: User): String {
		return locale.format(first.asMention, second.asMention) { commands.actions.kiss.response }
	}

	override fun getFolderName(): String {
		return "kiss"
	}

	override fun getEmoji(): String {
		return "\uD83D\uDC8F"
	}
}