package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.core.entities.User

class SlapCommand : ActionCommand("slap", listOf("tapa")) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.format { commands.actions.slap.description }
	}

	override fun getResponse(locale: LegacyBaseLocale, first: User, second: User): String {
		return if (second.id != Loritta.config.clientId) {
			locale.format(first.asMention, second.asMention) { commands.actions.slap.response }
		} else {
			// Quem tentar estapear a Loritta, vai ser estapeado
			locale.format(second.asMention, first.asMention) { commands.actions.slap.responseAntiIdiot }
		}
	}

	override fun getFolderName(): String {
		return "slap"
	}

	override fun getEmoji(): String {
		return "\uD83D\uDE40"
	}
}