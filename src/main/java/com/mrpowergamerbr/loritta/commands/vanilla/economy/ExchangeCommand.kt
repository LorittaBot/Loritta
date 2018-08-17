package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor

class ExchangeCommand : AbstractCommand("exchange", listOf("câmbio", "câmbiar", "lsx", "lsxs"), CommandCategory.ECONOMY) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["EXCHANGE_Description"]
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.config.economyConfig.exchangeRate != null) {
			val arg0 = context.rawArgs.getOrNull(0)

			if (arg0 == null) {
				context.reply(
						LoriReply(
								"**LorittaLand Sonhos Exchange Service (LSX)**",
								"\uD83D\uDD00"
						),
						LoriReply(
								"**Um sonho** equivalem a **${context.config.economyConfig.exchangeRate} ${context.config.economyConfig.economyNamePlural}**",
								"<:wow:432531424671694849>",
								mentionUser = false
						),
						LoriReply(
								"Você apenas pode câmbiar de sonhos para ${context.config.economyConfig.economyNamePlural}!",
								"⚠",
								mentionUser = false
						),
						LoriReply(
								"Para iniciar o câmbio, use `${context.config.commandPrefix}exchange QuantidadeDeSonhos`",
								mentionUser = false
						)
				)
				return
			} else {
				val howMuch = context.rawArgs.getOrNull(0)?.toDoubleOrNull()

				if (howMuch == null || howMuch.isNaN()) {
					context.reply(
							LoriReply(
									locale["INVALID_NUMBER", context.rawArgs[1]],
									Constants.ERROR
							)
					)
					return
				}

				if (0 >= howMuch || context.lorittaUser.profile.dreams.isNaN()) {
					context.reply(
							LoriReply(
									locale["INVALID_NUMBER", context.rawArgs[1]],
									Constants.ERROR
							)
					)
					return
				}

				val balanceQuantity = context.lorittaUser.profile.dreams

				// Se o servidor tem uma economia local...
				if (howMuch > balanceQuantity) {
					context.reply(
							LoriReply(
									locale["PAY_InsufficientFunds", locale["ECONOMY_NamePlural"]],
									Constants.ERROR
							)
					)
					return
				}

				val message = context.reply(
						LoriReply(
								"Você está prestes a câmbiar **${howMuch} Sonhos** para **${howMuch * context.config.economyConfig.exchangeRate!!} ${context.config.economyConfig.economyNamePlural}**! Para confirmar, clique em ✅",
								"\uD83D\uDCB8"
						),
						LoriReply(
								"**Atenção:** Você não poderá câmbiar seus sonhos de volta!",
								"⚠",
								mentionUser = false
						)
				)

				message.onReactionAddByAuthor(context) {
					if (it.reactionEmote.name == "✅") {
						message.delete().complete()

						val userProfile = loritta.getLorittaProfileForUser(context.userHandle.id)

						if (howMuch.isNaN()) {
							context.reply(
									LoriReply(
											locale["INVALID_NUMBER", context.rawArgs[1]],
											Constants.ERROR
									)
							)
							return@onReactionAddByAuthor
						}

						if (0 >= howMuch || context.lorittaUser.profile.dreams.isNaN()) {
							context.reply(
									LoriReply(
											locale["INVALID_NUMBER", context.rawArgs[1]],
											Constants.ERROR
									)
							)
							return@onReactionAddByAuthor
						}

						val balanceQuantity = userProfile.dreams

						// Se o servidor tem uma economia local...
						if (howMuch > balanceQuantity) {
							context.reply(
									LoriReply(
											locale["PAY_InsufficientFunds", locale["ECONOMY_NamePlural"]],
											Constants.ERROR
									)
							)
							return@onReactionAddByAuthor
						}

						loritta.usersColl.updateOne(
								Filters.eq("_id", userProfile.userId),
								Updates.inc("dreams", -howMuch)
						)

						loritta.serversColl.updateOne(
								Filters.and(
										Filters.eq(
												"_id", context.guild.id
										),
										Filters.eq(
												"guildUserData.userId", context.userHandle.id
										)
								),
								Updates.inc(
										"guildUserData.$.money", (howMuch * context.config.economyConfig.exchangeRate!!)
								)
						)

						context.reply(
								LoriReply(
										"Câmbio realizado com sucesso!"
								)
						)
					}
				}

				message.addReaction("✅").complete()
			}
		} else {
			context.reply(
					LoriReply(
							"Infelizmente este servidor não suporta câmbio de sonhos para ${context.config.economyConfig.economyNamePlural}... desculpe a inconveniência...",
							Constants.ERROR
					)
			)
		}
	}
}