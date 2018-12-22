package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.core.entities.User

class DanceCommand : ActionCommand("dance", listOf("dançar")) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.format { commands.actions.dance.description }
	}

	override fun getResponse(locale: LegacyBaseLocale, first: User, second: User): String {
		return locale.format(first.asMention, second.asMention) { commands.actions.dance.response }
	}

	override fun getFolderName(): String {
		return "dance"
	}

	override fun getEmoji(): String {
		return "\uD83D\uDD7A"
	}
}