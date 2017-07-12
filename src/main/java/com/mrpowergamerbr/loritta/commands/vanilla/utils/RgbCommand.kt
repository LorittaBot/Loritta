package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import java.awt.Color


class RgbCommand : CommandBase() {
	override fun getLabel(): String {
		return "rgb"
	}

	override fun getUsage(): String {
		return "hexadecimal"
	}

	override fun getDescription(): String {
		return "Transforme uma cor hexadecimal para RGB"
	}

	override fun getExample(): List<String> {
		return listOf("#bd8360");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext) {
		if (context.args.size == 1) {
			var hex = context.args[0]

			if (!hex.startsWith("#")) { hex.prependIndent("#"); } // Se não tem # antes, então adicione!

			try {
				var color = Color.decode(hex);
				context.sendMessage(context.getAsMention(true) + "Transformei a sua cor `$hex` para RGB! ${color.red}, ${color.green}, ${color.blue}")
			} catch (e: Exception) {
				context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + "A cor `$hex` não é uma cor hexadecimal válida!")
				return;
			}
		} else {
			this.explain(context);
		}
	}
}