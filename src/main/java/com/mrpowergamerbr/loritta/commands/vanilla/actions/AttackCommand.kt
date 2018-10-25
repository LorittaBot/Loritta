package com.mrpowergamerbr.loritta.commands.vanilla.actions

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.User

class AttackCommand : ActionCommand("attack", listOf("atacar")) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["ATTACK_Description"]
	}

	override fun getResponse(locale: BaseLocale, first: User, second: User): String {
		return locale["Attack_Response", first.asMention, second.asMention]
	}

	override fun getFolderName(): String {
		return "attack"
	}

	override fun getEmoji(): String {
		return "\uD83E\uDD4A"
	}
}