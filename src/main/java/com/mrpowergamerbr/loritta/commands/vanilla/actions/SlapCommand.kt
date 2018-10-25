package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.User

class SlapCommand : ActionCommand("slap", listOf("tapa")) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["SLAP_Description"]
	}

	override fun getResponse(locale: BaseLocale, first: User, second: User): String {
		return locale["SLAP_Response", first.asMention, second.asMention]
	}

	override fun getFolderName(): String {
		return "slap"
	}

	override fun getEmoji(): String {
		return "\uD83D\uDE40"
	}
}