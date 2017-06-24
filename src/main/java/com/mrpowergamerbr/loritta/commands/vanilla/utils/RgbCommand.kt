package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import java.awt.Color


class RgbCommand : CommandBase() {
	override fun getLabel(): String {
		return "rgb"
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

			if (hex.endsWith("#")) { hex.prependIndent("#"); } // Se não tem # antes, então adicione!

			var color = Color.decode(hex);

			context.sendMessage(context.getAsMention(true) + "Transformei a sua cor `$hex` para RGB! ${color.red}, ${color.green}, ${color.blue}")
		} else {
			this.explain(context);
		}
	}
}