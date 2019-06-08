package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale

class CaraCoroaCommand : AbstractCommand("flipcoin", listOf("girarmoeda", "flip", "toss", "moeda", "coin", "flipcoin"), CommandCategory.FUN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["CARACOROA_DESCRIPTION"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		context.sendMessage(context.getAsMention(true) + if (Loritta.RANDOM.nextBoolean()) "<:cara:412586256409559041> **${locale["CARACOROA_HEADS"]}!**" else "<:coroa:412586257114464259> **${locale["CARACOROA_TAILS"]}!**")
	}
}
