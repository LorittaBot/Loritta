package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.Emotes
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

			val economyConfig = transaction(Databases.loritta) {
				loritta.getOrCreateServerConfig(context.guild.idLong).economyConfig
			}

			val localEconomyEnabled = economyConfig?.enabled == true

			if (localEconomyEnabled && economyConfig != null) {
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
									"`${context.config.commandPrefix}pay local $display` — Forma de pagamento: ${economyConfig.economyNamePlural} (Você possui **${payerProfile.money} ${economyConfig.economyNamePlural}**!)",
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
								locale["PAY_InsufficientFunds", if (economySource == "global") locale["ECONOMY_NamePlural"] else economyConfig?.economyNamePlural],
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

				val activeMoneyFromDonations = loritta.getActiveMoneyFromDonations(context.userHandle.idLong)
				val taxBypass = activeMoneyFromDonations >= LorittaPrices.NO_PAY_TAX

				val taxedMoney = if (taxBypass) { 0.0 } else { Math.ceil(TRANSACTION_TAX * howMuch.toDouble()) }
				val finalMoney = howMuch - taxedMoney

				val message = context.reply(
						if (!taxBypass) {
							LoriReply(
									"Você irá transferir $howMuch sonhos ($taxedMoney sonhos de taxa) para ${user.asMention}! Ele irá receber ${howMuch - taxedMoney} sonhos"
							)
						} else {
							LoriReply(
									"Você irá transferir $howMuch sonhos para ${user.asMention}! Como você é um incrível doador, você não precisará pagar nenhuma taxa! Afinal, se você me ajudou, então você não precisa de taxas toscas te perturbando. ${Emotes.LORI_SMILE}"
							)
						},
						LoriReply(
								"Clique em ✅ para aceitar a transação!"
						)
				)

				message.onReactionAddByAuthor(context) {
					if (it.reactionEmote.name == "✅") {
						logger.info { "Sending request to transfer sonhos between ${context.userHandle.id} and ${user.id}, $howMuch sonhos will be transferred. Is mutex locked? ${mutex.isLocked}" }
						mutex.withLock {
							val shard = loritta.config.clusters.first { it.id == 1L }

							val body = HttpRequest.post("https://${shard.getUrl()}/api/v1/loritta/transfer-balance")
									.userAgent(loritta.lorittaCluster.getUserAgent())
									.header("Authorization", loritta.lorittaInternalApiKey.name)
									.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
									.readTimeout(loritta.config.loritta.clusterReadTimeout)
									.send(
											gson.toJson(
													jsonObject(
															"giverId" to context.userHandle.idLong,
															"receiverId" to user.idLong,
															"howMuch" to howMuch
													)
											)
									)
									.body()

							val result = jsonParser.parse(
									body
							).obj

							val status = PayStatus.valueOf(result["status"].string)

							if (status == PayStatus.SUCCESS) {
								val finalMoney = result["finalMoney"].double
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
				}

				message.addReaction("✅").queue()
			} else {
				val receiverProfile = context.config.getUserData(user.idLong)

				val beforeGiver = payerProfile.money
				val beforeReceiver = receiverProfile.money

				transaction(Databases.loritta) {
					payerProfile.money -= howMuch.toBigDecimal()
					receiverProfile.money += howMuch.toBigDecimal()
				}

				logger.info("${context.userHandle.id} (antes possuia ${beforeGiver} economia local) transferiu ${howMuch} economia local para ${receiverProfile.userId} (antes possuia ${beforeReceiver} economia local)")

				context.reply(
						LoriReply(
								locale["PAY_TransactionComplete", user.asMention, howMuch, if (howMuch == 1.0) { economyConfig?.economyName } else { economyConfig?.economyNamePlural }],
								"\uD83D\uDCB8"
						)
				)
			}
		} else {
			context.explain()
		}
	}

	enum class PayStatus {
		INVALID_MONEY_STATUS,
		NOT_ENOUGH_MONEY,
		SUCCESS
	}
}
