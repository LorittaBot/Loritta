package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ColorUtils
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat

class HexCommand : CommandBase("hex") {
	override fun getUsage(): String {
		return "vermelho verde azul"
	}

	override fun getExample(): List<String> {
		return listOf("255 165 0")
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.HEX_DESCRIPTION
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

				context.sendMessage(context.getAsMention(true) + context.locale.HEX_RESULT.msgFormat(r, g, b, ColorUtils().getColorNameFromRgb(r, g, b), hex))
			} catch (e: Exception) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.HEX_BAD_ARGS)
			}
		} else {
			context.explain()
		}
	}
}
