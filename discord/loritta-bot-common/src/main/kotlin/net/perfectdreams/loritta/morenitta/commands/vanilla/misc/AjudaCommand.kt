package net.perfectdreams.loritta.morenitta.commands.vanilla.misc

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.vanilla.utils.HelpCommand

class AjudaCommand(loritta: LorittaBot) : AbstractCommand(loritta, "ajuda", listOf("help", "comandos", "commands"), net.perfectdreams.loritta.common.commands.CommandCategory.MISC) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.help.description")

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "help")

		HelpCommand.executeCompat(CommandContextCompat.LegacyMessageCommandContextCompat(context))
	}
}