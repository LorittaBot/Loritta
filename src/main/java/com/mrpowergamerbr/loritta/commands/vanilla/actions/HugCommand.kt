package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.core.entities.User

class HugCommand : ActionCommand("hug", listOf("abraço", "abraçar", "abraco", "abracar")) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.format { commands.actions.hug.description }
	}

	override fun getResponse(locale: LegacyBaseLocale, first: User, second: User): String {
		return locale.format(first.asMention, second.asMention) { commands.actions.hug.response }
	}

	override fun getFolderName(): String {
		return "hug"
	}

	override fun getEmoji(): String {
		return "\uD83D\uDC99"
	}
}