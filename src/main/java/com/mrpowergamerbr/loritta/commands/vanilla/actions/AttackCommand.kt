package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.core.entities.User

class AttackCommand : ActionCommand("attack", listOf("atacar")) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.format { commands.actions.attack.description }
	}

	override fun getResponse(locale: LegacyBaseLocale, first: User, second: User): String {
		return if (second.id != Loritta.config.clientId) {
			locale.format(first.asMention, second.asMention) { commands.actions.attack.response }
		} else {
			// Quem tentar atacar a Loritta, vai levar umas porrada
			locale.format(second.asMention, first.asMention) { commands.actions.attack.responseAntiIdiot }
		}
	}

	override fun getFolderName(): String {
		return "attack"
	}

	override fun getEmoji(): String {
		return "\uD83E\uDD4A"
	}
}