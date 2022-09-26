package net.perfectdreams.loritta.morenitta.commands.vanilla.utils

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

class MorseCommand : AbstractCommand("morse", category = net.perfectdreams.loritta.common.commands.CommandCategory.UTILS) {
	// TODO: Fix Usage

	override fun getDescriptionKey() = LocaleKeyData("commands.command.morse.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.morse.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "morse")
	}
}