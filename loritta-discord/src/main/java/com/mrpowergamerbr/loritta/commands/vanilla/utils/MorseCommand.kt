package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.fromMorse
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.toMorse
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

class MorseCommand : AbstractCommand("morse", category = CommandCategory.UTILS) {
	override fun getUsage(): String {
		return "código morse ou texto"
	}

	override fun getExamples(): List<String> {
		return listOf("Loritta")
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["MORSE_DESCRIPTION"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val message = context.args.joinToString(" ")

			val toMorse = message.toUpperCase().toMorse()
			val fromMorse = message.fromMorse()

			if (toMorse.trim().isEmpty()) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["MORSE_FAIL"])
				return
			}

			val embed = EmbedBuilder()

			embed.setTitle(if (fromMorse.isNotEmpty()) "\uD83D\uDC48\uD83D\uDCFB ${locale["MORSE_TO_FROM"]}" else "\uD83D\uDC49\uD83D\uDCFB ${locale["MORSE_FROM_TO"]}")
			embed.setDescription("*beep* *boop*```${if (fromMorse.isNotEmpty()) fromMorse else toMorse}```")
			embed.setColor(Color(153, 170, 181))

			context.sendMessage(context.getAsMention(true), embed.build())
		} else {
			context.explain()
		}
	}
}
