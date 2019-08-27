package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.sync.Mutex
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class PagarCommand : AbstractCommand("pay", listOf("pagar"), CommandCategory.ECONOMY) {
	companion object {
		const val TRANSACTION_TAX = 0.05
		private val mutex = Mutex()
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["PAY_Description"]
	}

	override fun getUsage(): String {
		return "usuário quantia"
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.rawArgs.size >= 2) {
			var economySource = "global"
			var currentIdx = 0

			val payerProfile = context.config.getUserData(context.userHandle.idLong)

			if (context.config.economyConfig.isEnabled) {
				val arg0 = context.rawArgs.getOrNull(currentIdx++)

				if (arg0?.equals("global", true) == true || arg0?.equals("local", true) == true) {
					economySource = arg0
				} else {
					val strippedArgs = context.strippedArgs.toMutableList()

					var display = strippedArgs.joinToString(" ")

					if (context.rawArgs.isEmpty()) {
						display = "usuário quantia"
					}

					// Fonte não encontrada!
					context.reply(
							LoriReply(
									"Você precisa especificar qual será a forma de pagamento!",
									Constants.ERROR
							),
							LoriReply(
									"`${context.config.commandPrefix}pay global $display` — Forma de pagamento: Sonhos (Você possui **${context.lorittaUser.profile.money} Sonhos**!)",
									prefix = "<:loritta:331179879582269451>",
									mentionUser = false
							),
							LoriReply(
									"`${context.config.commandPrefix}pay local $display` — Forma de pagamento: ${context.config.economyConfig.economyNamePlural} (Você possui **${payerProfile.money} ${context.config.economyConfig.economyNamePlural}**!)",
									prefix = "\uD83D\uDCB5",
									mentionUser = false
							)
					)
					return
				}
			}

			val user = context.getUserAt(currentIdx++)
			val howMuch = context.rawArgs.getOrNull(currentIdx++)?.toDoubleOrNull()

			if (user == null || context.userHandle == user) {
				context.reply(
						LoriReply(
								locale["REP_InvalidUser"],
								Constants.ERROR
						)
				)
				return
			}

			if (howMuch == null || howMuch.isNaN()) {
				context.reply(
						LoriReply(
								locale["INVALID_NUMBER", context.rawArgs[1]],
								Constants.ERROR
						)
				)
				return
			}

			if (1 > howMuch || context.lorittaUser.profile.money.isNaN()) {
				context.reply(
						LoriReply(
								locale["INVALID_NUMBER", context.rawArgs[1]],
								Constants.ERROR
						)
				)
				return
			}

			// Se o servidor tem uma economia local...
			val balanceQuantity = if (economySource == "global") {
				BigDecimal(context.lorittaUser.profile.money)
			} else {
				payerProfile.money
			}

			if (howMuch.toBigDecimal() > balanceQuantity) {
				context.reply(
						LoriReply(
								locale["PAY_InsufficientFunds", if (economySource == "global") locale["ECONOMY_NamePlural"] else context.config.economyConfig.economyNamePlural],
								Constants.ERROR
						)
				)
				return
			}

			// Hora de transferir!
			if (economySource == "global") {
				val epochMillis = context.userHandle.timeCreated.toEpochSecond() * 1000

				if (epochMillis + Constants.ONE_WEEK_IN_MILLISECONDS > System.currentTimeMillis()) { // 7 dias
					context.reply(
							LoriReply(
									"Você não pode transferir sonhos! Tente novamente mais tarde...",
									Constants.ERROR
							)
					)
					return
				}

				val taxedMoney = Math.ceil(TRANSACTION_TAX * howMuch.toDouble())
				val finalMoney = howMuch - taxedMoney

				val message = context.reply(
						LoriReply(
								"Você irá transferir $howMuch sonhos ($taxedMoney sonhos de taxa) para ${user.asMention}! Ele irá receber ${howMuch - taxedMoney} sonhos"
						),
						LoriReply(
								"Clique em ✅ para aceitar a transação!"
						)
				)

				message.onReactionAddByAuthor(context) {
					if (it.reactionEmote.name == "✅") {
						mutex.withLock {
							val receiverProfile = loritta.getOrCreateLorittaProfile(user.id)

							if (receiverProfile.money.isNaN()) {
								// receiverProfile.dreams = 0.0
								return@onReactionAddByAuthor
							}

							if (context.lorittaUser.profile.money.isNaN()) {
								// context.lorittaUser.profile.dreams = 0.0
								return@onReactionAddByAuthor
							}

							val giverProfile = loritta.getOrCreateLorittaProfile(context.lorittaUser.profile.id.value)

							if (howMuch > giverProfile.money)
								return@onReactionAddByAuthor

							val beforeGiver = context.lorittaUser.profile.money
							val beforeReceiver = receiverProfile.money

							transaction(Databases.loritta) {
								context.lorittaUser.profile.money -= howMuch
								receiverProfile.money += finalMoney
							}

							logger.info("${context.userHandle.id} (antes possuia ${beforeGiver} sonhos) transferiu ${howMuch} sonhos para ${receiverProfile.userId} (antes possuia ${beforeReceiver} sonhos, recebeu apenas $finalMoney (taxado!))")

							context.reply(
									LoriReply(
											locale["PAY_TransactionComplete", user.asMention, finalMoney, if (finalMoney == 1.0) {
												locale["ECONOMY_Name"]
											} else {
												locale["ECONOMY_NamePlural"]
											}],
											"\uD83D\uDCB8"
									)
							)
						}
					}
				}

				message.addReaction("✅").queue()
			} else {
				val receiverProfile = context.config.getUserData(user.idLong)

				val beforeGiver = payerProfile.money
				val beforeReceiver = receiverProfile.money

				payerProfile.money -= howMuch.toBigDecimal()
				receiverProfile.money += howMuch.toBigDecimal()

				logger.info("${context.userHandle.id} (antes possuia ${beforeGiver} economia local) transferiu ${howMuch} economia local para ${receiverProfile.userId} (antes possuia ${beforeReceiver} economia local)")
				loritta save context.config

				context.reply(
						LoriReply(
								locale["PAY_TransactionComplete", user.asMention, howMuch, if (howMuch == 1.0) { context.config.economyConfig.economyName } else { context.config.economyConfig.economyNamePlural }],
								"\uD83D\uDCB8"
						)
				)
			}
		} else {
			context.explain()
		}
	}
}
