package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.User

class AttackCommand : ActionCommand("attack", listOf("atacar")) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.commands.actions.attack.description
	}

	override fun getResponse(locale: BaseLocale, first: User, second: User): String {
		return if (second.id != Loritta.config.clientId) {
			locale.commands.actions.attack.response.f(first.asMention, second.asMention)
		} else {
			// Quem tentar atacar a Loritta, vai levar umas porrada
			locale.commands.actions.attack.responseAntiIdiot.f(second.asMention, first.asMention)
		}
	}

	override fun getFolderName(): String {
		return "attack"
	}

	override fun getEmoji(): String {
		return "\uD83E\uDD4A"
	}
}