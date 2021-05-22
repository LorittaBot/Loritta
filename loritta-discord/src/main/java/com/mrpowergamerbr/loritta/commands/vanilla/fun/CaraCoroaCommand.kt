package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply

class CaraCoroaCommand : AbstractCommand("coinflip", listOf("girarmoeda", "flipcoin", "caracoroa"), CommandCategory.FUN) {
	companion object {
		const val LOCALE_PREFIX = "commands.command.flipcoin"
	}

	override fun getDescriptionKey() = LocaleKeyData("commands.command.flipcoin.description")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val isTails = Loritta.RANDOM.nextBoolean()
		val prefix: String
		val message: String

		if (isTails) {
			prefix = "<:coroa:412586257114464259>"
			message = context.locale["$LOCALE_PREFIX.tails"]
		} else {
			prefix = "<:cara:412586256409559041>"
			message = context.locale["$LOCALE_PREFIX.heads"]
		}

		context.reply(
                LorittaReply(
                        "**$message!**",
                        prefix
                )
		)
	}
}