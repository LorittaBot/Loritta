package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.User

class DanceCommand : ActionCommand("dance", listOf("dançar")) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.format { commands.actions.dance.description }
	}

	override fun getResponse(locale: BaseLocale, first: User, second: User): String {
		return locale.format(first.asMention, second.asMention) { commands.actions.dance.response }
	}

	override fun getFolderName(): String {
		return "dance"
	}

	override fun getEmoji(): String {
		return "\uD83D\uDD7A"
	}
}