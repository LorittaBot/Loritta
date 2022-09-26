package net.perfectdreams.loritta.legacy.commands.vanilla.utils

import net.perfectdreams.loritta.legacy.Loritta
import net.perfectdreams.loritta.legacy.commands.AbstractCommand
import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.common.locale.LocaleKeyData
import net.perfectdreams.loritta.legacy.utils.Emotes
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

class EscolherCommand : AbstractCommand("choose", listOf("escolher"), category = CommandCategory.UTILS) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.choose.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.choose.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "choose")

		if (context.args.isNotEmpty()) {
			val joined = context.args.joinToString(separator = " ") // Vamos juntar tudo em uma string
			val split = joined.split(",").map { it.trim() } // E vamos separar!

			// Hora de escolher algo aleatório!
			val chosen = split[Loritta.RANDOM.nextInt(split.size)]
			context.reply(
                    LorittaReply(
                            message = context.locale["commands.command.choose.result", chosen],
                            prefix = Emotes.LORI_HM
                    )
			)
		} else {
			context.explain()
		}
	}
}