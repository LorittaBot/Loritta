package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.fromMorse
import com.mrpowergamerbr.loritta.utils.toMorse
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color

class MorseCommand : CommandBase() {
	override fun getLabel(): String {
		return "morse"
	}

	override fun getUsage(): String {
		return "código morse ou texto"
	}

	override fun getExample(): List<String> {
		return listOf("Loritta")
	}

	override fun getDescription(): String {
		return "Codifica/Decodifica uma mensagem em código morse"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val message = context.args.joinToString(" ");

			val toMorse = message.toUpperCase().toMorse()
			val fromMorse = message.fromMorse()

			val embed = EmbedBuilder();

			embed.setTitle(if (fromMorse.isNotEmpty()) "\uD83D\uDC48\uD83D\uDCFB Morse para Texto" else "\uD83D\uDC49\uD83D\uDCFB Texto para Morse")
			embed.setDescription("*beep* *boop*```${if (fromMorse.isNotEmpty()) fromMorse else toMorse}```")
			embed.setColor(Color(153, 170, 181))

			context.sendMessage(context.getAsMention(true), embed.build())
		} else {
			context.explain()
		}
	}
}
