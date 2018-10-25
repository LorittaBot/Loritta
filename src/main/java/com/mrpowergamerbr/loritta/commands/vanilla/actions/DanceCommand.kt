package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.User

class DanceCommand : ActionCommand("dance", listOf("dançar")) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["DANCE_Description"]
	}

	override fun getResponse(locale: BaseLocale, first: User, second: User): String {
		return locale["DANCE_Response", first.asMention, second.asMention]
	}

	override fun getFolderName(): String {
		return "dance"
	}

	override fun getEmoji(): String {
		return "\uD83D\uDD7A"
	}
}