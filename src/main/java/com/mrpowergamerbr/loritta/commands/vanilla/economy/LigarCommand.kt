package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.frontend.views.LoriWebCodes
import com.mrpowergamerbr.loritta.threads.LoteriaThread
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.util.*

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

				if (loritta.bomDiaECia.available) {
					loritta.bomDiaECia.available = false

					val randomPrize = RANDOM.nextInt(100, 601)

					profile.dreams += randomPrize
					loritta save profile

					logger.info("${context.userHandle.id} ganhou ${randomPrize} no Bom Dia & Cia!")

					context.reply(
							LoriReply(
									"Rodamos a roleta e... Parabéns! Você ganhou **${randomPrize} Sonhos**!",
									"<:yudi:446394608256024597>"
							)
					)

					loritta.bomDiaECia.announceWinner(context.userHandle)
				} else {
					context.reply(
							LoriReply(
									"Poxa, não foi dessa vez amiguinho... mas não desista, ligue somente durante o programa, tá? Valeu! (Você apenas deve ligar após a <@297153970613387264> anunciar no chat para ligar!)",
									"<:yudi:446394608256024597>"
							)
					)
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