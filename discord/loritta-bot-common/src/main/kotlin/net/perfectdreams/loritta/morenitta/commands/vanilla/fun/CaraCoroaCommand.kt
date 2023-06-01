package net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`.CoinFlipCommand
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

class CaraCoroaCommand(loritta: LorittaBot) : AbstractCommand(loritta, "coinflip", listOf("girarmoeda", "flipcoin", "caracoroa"), net.perfectdreams.loritta.common.commands.CommandCategory.FUN) {
	companion object {
		const val LOCALE_PREFIX = "commands.command.flipcoin"
	}

	override fun getDescriptionKey() = LocaleKeyData("commands.command.flipcoin.description")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "coinflip")

		CoinFlipCommand.executeCompat(CommandContextCompat.LegacyMessageCommandContextCompat(context))
	}
}