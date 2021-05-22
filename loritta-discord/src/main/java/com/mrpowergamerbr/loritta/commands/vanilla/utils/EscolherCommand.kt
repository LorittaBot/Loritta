package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.Emotes

class EscolherCommand : AbstractCommand("choose", listOf("escolher"), category = CommandCategory.UTILS) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.choose.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.choose.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
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