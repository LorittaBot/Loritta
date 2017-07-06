package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ColorUtils

class HexCommand : CommandBase() {
	override fun getLabel(): String {
		return "hex"
	}

	override fun getUsage(): String {
		return "vermelho verde azul"
	}

	override fun getExample(): List<String> {
		return listOf("255 165 0")
	}

	override fun getDescription(): String {
		return "Transforme uma cor RGB para hexadecimal"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun run(context: CommandContext) {
		if (context.args.size == 3) {
			try {
				val r = Integer.parseInt(context.args[0].replace(",", ""))
				val g = Integer.parseInt(context.args[1].replace(",", ""))
				val b = Integer.parseInt(context.args[2].replace(",", ""))

				val hex = String.format("#%02x%02x%02x", r, g, b)

				context.sendMessage(context.getAsMention(true) + String.format("Transformei a sua cor %s, %s, %s (%s) para hexadecimal! %s", r, g, b, ColorUtils().getColorNameFromRgb(r, g, b), hex))
			} catch (e: Exception) {
				context.sendMessage(context.getAsMention(true) + "Todos os argumentos devem ser números!")
			}
		} else {
			context.explain()
		}
	}
}
