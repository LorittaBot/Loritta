package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import java.util.*

class RollCommand : CommandBase() {
	override fun getLabel(): String {
		return "rolar"
	}

	override fun getDescription(): String {
		return "Rola um dado e fala o resultado dele, perfeito quando você quer jogar um Monopoly maroto mas perdeu os dados."
	}

	override fun getAliases(): List<String> {
		return Arrays.asList("roll")
	}

	override fun getUsage(): String {
		return "[número]"
	}

	override fun getExample(): List<String> {
		return listOf("", "12", "24")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("número" to "*(Opcional)* Quantos lados o dado que eu irei rolar terá, padrão: 6")
	}

	override fun run(context: CommandContext) {
		var value: Long = 6
		if (context.args.size == 1) {
			try {
				value = context.args[0].toLong()
				Loritta.random.nextLong(1, value + 1)
			} catch (e: Exception) {
				context.sendMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + "Número `${context.args[0]}` é algo irreconhecível para um bot como eu, sorry. \uD83D\uDE22")
				return
			}

		}

		if (0 >= value) {
			context.sendMessage(context.getAsMention(true) + "Número inválido!")
			return
		}

		context.sendMessage(context.getAsMention(true) + "\uD83C\uDFB2 **Resultado:** " + Loritta.random.nextLong(1, value + 1))
	}
}