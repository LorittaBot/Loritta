package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Jankenpon
import com.mrpowergamerbr.loritta.utils.Jankenpon.JankenponStatus
import java.io.File
import java.io.IOException

class PedraPapelTesouraCommand : CommandBase() {
	override fun getLabel(): String {
		return "ppt"
	}

	override fun getDescription(): String {
		return "Jogue Pedra, Papel ou Tesoura! (jankenpon, ou a versão abrasileirada: jokenpô)"
	}

	override fun getUsage(): String {
		return "sua escolha"
	}

	override fun getExample(): List<String> {
		return listOf("pedra", "papel", "tesoura")
	}

	override fun getDetailedUsage(): Map<String, String> {
		return mapOf("sua escolha" to "Pedra, Papel ou Tesoura")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val playerValue = context.args[0]

			val janken = Jankenpon.getFromLangString(playerValue.toLowerCase())

			if (janken != null) {
				val opponent = Jankenpon.values()[Loritta.random.nextInt(Jankenpon.values().size)]

				val status = janken.getStatus(opponent)

				var fancy: String? = null
				if (status == JankenponStatus.WIN) {
					fancy = "**Parabéns, você ganhou! \uD83D\uDE0A**"
				}
				if (status == JankenponStatus.LOSE) {
					fancy = "**Que pena... você perdeu, mas o que vale é a intenção! \uD83D\uDE42**"
				}
				if (status == JankenponStatus.DRAW) {
					fancy = "**Empate! Que tal uma revanche? \uD83D\uDE0A**"
				}
				context.sendMessage(context.getAsMention(true) + "Você escolheu " + janken.emoji + ", eu escolhi " + opponent.emoji + "\n" + fancy)
			} else {
				if (playerValue.equals("jesus", ignoreCase = true)) {
					val fancy = "**Empate...? 🤔 🤷**"
					context.sendMessage(context.getAsMention(true) + "Você escolheu 🙇 *JESUS CRISTO*🙇, eu escolhi 🙇 *JESUS CRISTO*🙇\n" + fancy)
				} else if (playerValue.equals("velberan", ignoreCase = true)) {
					val opponent = Jankenpon.values()[Loritta.random.nextInt(Jankenpon.values().size)]

					val fancy = "🕹🕹🕹"
					context.sendMessage(context.getAsMention(true) + "Você escolheu 🕹 *VELBERAN*🕹, eu escolhi " + opponent.emoji + "\n" + fancy)
					try {
						context.sendFile(File(Loritta.FOLDER + "velberan.gif"), "velberan.gif", " ")
					} catch (e: IOException) {
						e.printStackTrace()
					}

				} else {
					val fancy = "**Que pena... você perdeu, dá próxima vez escolha algo que seja válido, ok? \uD83D\uDE09**"
					context.sendMessage(context.getAsMention(true) + "Você escolheu 💩, eu escolhi 🙇 *JESUS CRISTO*🙇\n" + fancy)
				}
			}
		} else {
			context.explain()
		}
	}
}