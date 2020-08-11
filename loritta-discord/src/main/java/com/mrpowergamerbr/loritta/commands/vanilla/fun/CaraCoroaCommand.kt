package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import org.jetbrains.exposed.sql.transactions.transaction

class CaraCoroaCommand : AbstractCommand("coinflip", listOf("girarmoeda", "flipcoin", "caracoroa"), CommandCategory.FUN) {
	companion object {
		const val LOCALE_PREFIX = "commands.fun.flipcoin"
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["CARACOROA_DESCRIPTION"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val lorittaProfile = context.lorittaUser.profile

		if (context.args.getOrNull(0) == "available") {
			val willBeAvailable = !lorittaProfile.availableForBets

			val reply: LoriReply = if (willBeAvailable) {
				LoriReply(
						context.locale["$LOCALE_PREFIX.betsTurnedOn"]
				)
			} else {
				LoriReply(
						context.locale["$LOCALE_PREFIX.betsTurnedOff"]
				)
			}

			transaction {
				lorittaProfile.availableForBets = willBeAvailable
			}

			context.reply(reply)
		}

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
				LoriReply(
						"**$message!**",
						prefix
				)
		)
	}
}
