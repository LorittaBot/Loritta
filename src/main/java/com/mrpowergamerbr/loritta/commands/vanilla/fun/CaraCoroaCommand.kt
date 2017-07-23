package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class CaraCoroaCommand : CommandBase() {
	override fun getLabel(): String {
		return "girarmoeda"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.CARACOROA_DESCRIPTION
	}

	override fun run(context: CommandContext) {
		context.sendMessage(context.getAsMention(true) + if (Loritta.random.nextBoolean()) "\uD83D\uDE46\u200D **${context.locale.CARACOROA_HEADS.f()}!**" else "\uD83D\uDC51 **${context.locale.CARACOROA_TAILS.f()}!**")
	}
}
