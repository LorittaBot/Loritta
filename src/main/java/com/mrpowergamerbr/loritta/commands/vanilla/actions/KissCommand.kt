package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.User

class KissCommand : ActionCommand("kiss", listOf("beijar", "beijo")) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["KISS_Description"]
	}

	override fun getResponse(locale: BaseLocale, first: User, second: User): String {
		return locale["KISS_Response", first.asMention, second.asMention]
	}

	override fun getFolderName(): String {
		return "kiss"
	}

	override fun getEmoji(): String {
		return "\uD83D\uDC8F"
	}
}