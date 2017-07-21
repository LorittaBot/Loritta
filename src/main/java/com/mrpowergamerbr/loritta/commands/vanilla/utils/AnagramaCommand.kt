package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat


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

	override fun getDescription(locale: BaseLocale): String {
		return locale.ANAGRAMA_DESCRIPTION
	}

	override fun getExtendedExamples(): Map<String, String> {
		return mapOf("Loritta" to "Cria um anagrama usando a palavra \"Loritta\"",
				"kk eae men" to "Cria um anagrama usando a frase \"kk eae men\"")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val palavra = context.args.joinToString(separator = " ");

			val shuffledChars = LorittaUtilsKotlin.shuffle(palavra.toCharArray().toMutableList())

			val shuffledWord = shuffledChars.joinToString(separator = "");

			context.sendMessage("✍ **|** " + context.getAsMention(true) + "${context.locale.ANAGRAMA_RESULT.msgFormat(shuffledWord)} \uD83D\uDE4B")
		} else {
			this.explain(context);
		}
	}
}