package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.DailyCommand

class DailyCommand(loritta: LorittaBot) : AbstractCommand(loritta, "daily", listOf("diário", "bolsafamilia", "bolsafamília"), net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.daily.description")

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "daily")
		DailyCommand.executeCompat(CommandContextCompat.LegacyMessageCommandContextCompat(context))
	}
}