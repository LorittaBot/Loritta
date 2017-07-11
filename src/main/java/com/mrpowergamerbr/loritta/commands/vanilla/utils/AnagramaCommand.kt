package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin


class AnagramaCommand : CommandBase() {
	override fun getLabel(): String {
		return "anagrama"
	}

	override fun getUsage(): String {
		return "palavra"
	}

	override fun getAliases(): List<String> {
		return listOf("shuffle")
	}

	override fun getDescription(): String {
		return "Cria um anagrama de uma palavra!"
	}

	override fun getExample(): List<String> {
		return listOf("Loritta");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val palavra = context.args.joinToString(separator = " ");

			val shuffledChars = LorittaUtilsKotlin.shuffle(palavra.toCharArray().toMutableList())

			val shuffledWord = shuffledChars.joinToString(separator = "");

			context.sendMessage(context.getAsMention(true) + "Seu anagrama é... `$shuffledWord`")
		} else {
			this.explain(context);
		}
	}
}