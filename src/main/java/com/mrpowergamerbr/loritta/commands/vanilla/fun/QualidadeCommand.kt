package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext

class QualidadeCommand : CommandBase() {
	override fun getLabel(): String {
		return "qualidade"
	}

	override fun getDescription(): String {
		return "Cria uma mensagem com Q U A L I D A D E & S I N C R O N I A"
	}

	override fun getUsage(): String {
		return "<mensagem>"
	}

	override fun getExample(): List<String> {
		return listOf("qualidade & sincronia")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("mensagem" to "A mensagem que você deseja transformar")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val qualidade = context.args.joinToString(" ")

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