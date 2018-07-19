package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

class LigarCommand : AbstractCommand("ligar", category = CommandCategory.ECONOMY) {
	override fun getDescription(locale: BaseLocale): String {
		return "Experimental";
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val phoneNumber = context.args.getOrNull(0)?.replace("-", "")

		if (phoneNumber != null) {
			if (phoneNumber == "40028922") {
				val profile = context.lorittaUser.profile

				if (75 > profile.dreams) {
					context.reply(
							"Você não tem sonhos suficientes para completar esta ligação!",
							Constants.ERROR
					)
					return
				}

				profile.dreams -= 75
				loritta save profile

				synchronized(this) {
					if (loritta.bomDiaECia.available) {
						val args = context.args.toMutableList()
						args.removeAt(0)
						val text = args.joinToString(" ")
								.toLowerCase()

						if (text.contains("\u200B") || text.contains("\u200C") || text.contains("\u200D")) {
							context.reply(
									LoriReply(
											"Poxa, não foi dessa vez amiguinho... mas não desista, ligue somente durante o programa, tá? Valeu! Aliás, não utilize CTRL-C e CTRL-V para você tentar vencer mais rápido. :^)",
											"<:yudi:446394608256024597>"
									)
							)
							return@synchronized
						}

						if (text != loritta.bomDiaECia.currentText) {
							context.reply(
									LoriReply(
											"Poxa, não foi dessa vez amiguinho... mas não desista, ligue somente durante o programa, tá? Valeu! Não se esqueça de escrever a nossa frase para que você possa ganhar o prêmio!",
											"<:yudi:446394608256024597>"
									)
							)
							return@synchronized
						}

						loritta.bomDiaECia.available = false

						val randomPrize = RANDOM.nextInt(150, 376)

						profile.dreams += randomPrize
						loritta save profile

						logger.info("${context.userHandle.id} ganhou ${randomPrize} no Bom Dia & Cia!")

						context.reply(
								LoriReply(
										"Rodamos a roleta e... Parabéns! Você ganhou **${randomPrize} Sonhos**!",
										"<:yudi:446394608256024597>"
								)
						)

						loritta.bomDiaECia.announceWinner(context.guild, context.userHandle)
					} else {
						context.reply(
								LoriReply(
										"Poxa, não foi dessa vez amiguinho... mas não desista, ligue somente durante o programa, tá? Valeu! (Você apenas deve ligar após a <@297153970613387264> anunciar no chat para ligar!)",
										"<:yudi:446394608256024597>"
								)
						)
					}
				}
			} else {
				context.reply(
						"Número desconhecido",
						"\uD83D\uDCF4"
				)
			}
		} else {
			this.explain(context)
		}
	}
}