package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.misc.VaporwaveUtils

class VaporQualidadeCommand : CommandBase() {
	override fun getLabel(): String {
		return "vaporqualidade"
	}

	override fun getDescription(): String {
		return "Quando você mistura Q U A L I D A D E e ａｅｓｔｈｅｔｉｃｓ"
	}

	override fun getUsage(): String {
		return "<mensagem>"
	}

	override fun getExample(): List<String> {
		return listOf("kk eae men, o sam é brabo")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("mensagem" to "A mensagem que você deseja transformar")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val qualidade = VaporwaveUtils.vaporwave(context.args.joinToString(" "))

			val sb = StringBuilder()
			for (ch in qualidade.toCharArray()) {
				if (Character.isLetterOrDigit(ch)) {
					sb.append(Character.toUpperCase(ch))
					sb.append(" ")
				} else {
					sb.append(ch)
				}
			}

			context.sendMessage(context.getAsMention(true) + sb.toString())
		} else {
			this.explain(context)
		}
	}
}