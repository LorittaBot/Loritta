package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import java.awt.Color


class RgbCommand : CommandBase("rgb") {
	override fun getUsage(): String {
		return "hexadecimal"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.RGB_DESCRIPTION
	}

	override fun getExample(): List<String> {
		return listOf("#bd8360");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.size == 1) {
			var hex = context.args[0]

			if (!hex.startsWith("#")) { hex = "#$hex"; } // Se não tem # antes, então adicione!

			try {
				var color = Color.decode(hex);
				context.sendMessage(context.getAsMention(true) + context.locale.RGB_TRANSFORMED.msgFormat(hex, color.red, color.green, color.blue))
			} catch (e: Exception) {
				context.sendMessage(Constants.ERROR + " **|** " + context.locale.RGB_INVALID.msgFormat(hex))
				return;
			}
		} else {
			this.explain(context);
		}
	}
}