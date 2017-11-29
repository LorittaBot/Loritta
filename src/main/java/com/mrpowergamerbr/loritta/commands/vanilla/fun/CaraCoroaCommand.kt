package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class CaraCoroaCommand : CommandBase("girarmoeda") {
	override fun getDescription(locale: BaseLocale): String {
		return locale["CARACOROA_DESCRIPTION"]
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		context.sendMessage(context.getAsMention(true) + if (Loritta.random.nextBoolean()) "<:cara:345994349969932291> **${context.locale.CARACOROA_HEADS.f()}!**" else "<:coroa:345994350498545674> **${context.locale.CARACOROA_TAILS.f()}!**")
	}
}
