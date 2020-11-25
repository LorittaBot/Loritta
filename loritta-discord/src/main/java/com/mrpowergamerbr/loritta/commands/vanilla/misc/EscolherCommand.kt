package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes

class EscolherCommand : AbstractCommand("choose", listOf("escolher"), category = CommandCategory.MISC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.misc.choose.description"]
	}

	override fun getExamples(): List<String> {
		return listOf("Sonic, Tails, Knuckles", "Asriel Dreemurr, Chara Dreemurr", "Shantae, Risky Boots")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val joined = context.args.joinToString(separator = " ") // Vamos juntar tudo em uma string
			val split = joined.split(",").map { it.trim() } // E vamos separar!

			// Hora de escolher algo aleatório!
			val chosen = split[Loritta.RANDOM.nextInt(split.size)]
			context.reply(
                    LorittaReply(
                            message = context.locale["commands.misc.choose.result", chosen],
                            prefix = Emotes.LORI_HM
                    )
			)
		} else {
			context.explain()
		}
	}
}