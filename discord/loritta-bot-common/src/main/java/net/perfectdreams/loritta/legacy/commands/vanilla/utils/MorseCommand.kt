package net.perfectdreams.loritta.legacy.commands.vanilla.utils

import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.Constants
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.text.MorseUtils
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils
import java.awt.Color

class MorseCommand : AbstractCommand("morse", category = net.perfectdreams.loritta.common.commands.CommandCategory.UTILS) {
	// TODO: Fix Usage

	override fun getDescriptionKey() = LocaleKeyData("commands.command.morse.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.morse.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "morse")
	}
}