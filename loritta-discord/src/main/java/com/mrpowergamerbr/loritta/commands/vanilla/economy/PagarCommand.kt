package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.gson
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionAdd
import com.mrpowergamerbr.loritta.utils.removeAllFunctions
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.AccountUtils
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.NumberUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class PagarCommand : AbstractCommand("pay", listOf("pagar"), CommandCategory.ECONOMY) {
	companion object {
		private val mutex = Mutex()
	}

	override fun getDescriptionKey() = LocaleKeyData("commands.command.pay.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.pay.examples")

	// TODO: Fix Usage

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
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
							LorittaReply(
									"Você precisa especificar qual será a forma de pagamento!",
									Constants.ERROR
							),
							LorittaReply(
									"`${context.config.commandPrefix}pay global $display` — Forma de pagamento: Sonhos (Você possui **${context.lorittaUser.profile.money} Sonhos**!)",
									prefix = "<:loritta:331179879582269451>",
									mentionUser = false
							),
							LorittaReply(
									"`${context.config.commandPrefix}pay local $display` — Forma de pagamento: ${economyConfig.economyNamePlural} (Você possui **${payerProfile.money} ${economyConfig.economyNamePlural}**!)",
									prefix = "\uD83D\uDCB5",
									mentionUser = false
							)
					)
					return
				}
			}

			val user = context.getUserAt(currentIdx++)
			val arg1 = context.rawArgs.getOrNull(currentIdx++) ?: run {
				explain(context)
				return
			}

			if (user == null || context.userHandle == user) {
				context.reply(
						LorittaReply(
								locale["commands.userDoesNotExist", context.rawArgs[0].stripCodeMarks()],
								Constants.ERROR
						)
				)
				return
			}

			val howMuch = NumberUtils.convertShortenedNumberToLong(arg1)

			if (howMuch == null) {
				context.reply(
						LorittaReply(
								locale["commands.invalidNumber", arg1],
								Constants.ERROR
						)
				)
				return
			}

			if (1 > howMuch) {
				context.reply(
						LorittaReply(
								locale["commands.invalidNumber", context.rawArgs[1]],
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
						LorittaReply(
								locale["commands.command.pay.insufficientFunds", if (economySource == "global") locale["economy.currency.name.plural"] else economyConfig?.economyNamePlural],
								Constants.ERROR
						)
				)
				return
			}

			// Hora de transferir!
			if (economySource == "global") {
				// User checks
				if (!checkIfSelfAccountIsOldEnough(context))
					return
				if (!checkIfOtherAccountIsOldEnough(context, user))
					return
				if (!checkIfSelfAccountGotDailyRecently(context))
					return

				var tellUserLorittaIsGrateful = false
				val userProfile = loritta.getOrCreateLorittaProfile(user.idLong)

				if (user.idLong == loritta.discordConfig.discord.clientId.toLong()) {
					// If it is Loritta, she doesn't want to *feel* that she is poor if she is rich
					// So, to do that, the check is dynamic
					// If she has 1_000_000 sonhos, she will want *at least* 100_000 sonhos
					//
					// To do that is *easy*, just multiply how much sonhos she has by 0.1 and, if the value is below the threshold, deny the sonhos.
					val threshold = userProfile.money * 0.1

					if (25_000 >= userProfile.money) {
						// If Loritta has almost no sonhos (less than 25k), Loritta will tell the user that she is very grateful for the donation!
						tellUserLorittaIsGrateful = true
					} else if (threshold > howMuch) {
						// If the user is trying to give not enough sonhos, Loritta will think that the user thinks she is poor and will
						// reject the sonhos
						context.reply(
								LorittaReply(
										context.locale["commands.command.pay.doYouThinkImPoor"],
										Emotes.LORI_BAN_HAMMER
								)
						)
						return
					}
				} else {
					if (AccountUtils.checkAndSendMessageIfUserIsBanned(context, userProfile))
						return
				}

				val quirkyMessage = when {
					howMuch >= 500_000 -> " ${context.locale.getList("commands.command.pay.randomQuirkyRichMessages").random()}"
					tellUserLorittaIsGrateful -> " ${context.locale.getList("commands.command.pay.randomLorittaIsGratefulMessages").random()}"
					else -> ""
				}

				val message = context.reply(
						LorittaReply(
								context.locale["commands.command.pay.youAreGoingToTransfer", howMuch, user.asMention, quirkyMessage],
								Emotes.LORI_RICH
						),
						LorittaReply(
								context.locale["commands.command.pay.clickToAcceptTheTransaction", user.asMention, "✅"],
								"🤝",
								mentionUser = false
						),
						LorittaReply(
								context.locale["commands.command.pay.sellDisallowedWarning", "${loritta.instanceConfig.loritta.website.url}guidelines"],
								Emotes.LORI_BAN_HAMMER,
								mentionUser = false
						)
				)

				message.onReactionAdd(context) {
					if (it.reactionEmote.name == "✅") {
						mutex.withLock {
							// Multiple users can click on the message at the same time, so inside the mutex we need to check if the
							// message is still in the interaction cache.
							//
							// If it isn't, then it means that the message was already processed!
							if (loritta.messageInteractionCache.containsKey(it.messageIdLong)) {
								val usersThatReactedToTheMessage = it.reaction.retrieveUsers().await()

								if (context.userHandle in usersThatReactedToTheMessage && user in usersThatReactedToTheMessage) {
									message.removeAllFunctions()

									logger.info { "Sending request to transfer sonhos between ${context.userHandle.id} and ${user.id}, $howMuch sonhos will be transferred. Is mutex locked? ${mutex.isLocked}" }
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

									val result = JsonParser.parseString(
											body
									).obj

									val status = PayStatus.valueOf(result["status"].string)

									if (status == PayStatus.SUCCESS) {
										val finalMoney = result["finalMoney"].double
										context.reply(
												LorittaReply(
														locale["commands.command.pay.transitionComplete", user.asMention, finalMoney, if (finalMoney == 1.0) {
															locale["economy.currency.name.singular"]
														} else {
															locale["economy.currency.name.plural"]
														}],
														"\uD83D\uDCB8"
												)
										)
									}
								}
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
						LorittaReply(
								locale["commands.command.pay.transitionComplete", user.asMention, howMuch, if (howMuch.toLong() == 1L) {
									economyConfig?.economyName
								} else {
									economyConfig?.economyNamePlural
								}],
								"\uD83D\uDCB8"
						)
				)
			}
		} else {
			context.explain()
		}
	}

	private suspend fun checkIfSelfAccountGotDailyRecently(context: CommandContext): Boolean {
		// Check if the user got daily in the last 14 days before allowing a transaction
		val dailyRewardInTheLastXDays = AccountUtils.getUserDailyRewardInTheLastXDays(context.lorittaUser.profile, 14)

		if (dailyRewardInTheLastXDays == null) {
			context.reply(
					LorittaReply(
							context.locale["commands.youNeedToGetDailyRewardBeforeDoingThisAction", context.config.commandPrefix],
							Constants.ERROR
					)
			)
			return false
		}
		return true
	}

	private suspend fun checkIfSelfAccountIsOldEnough(context: CommandContext): Boolean {
		val epochMillis = context.userHandle.timeCreated.toEpochSecond() * 1000

		if (epochMillis + (Constants.ONE_WEEK_IN_MILLISECONDS * 2) > System.currentTimeMillis()) { // 14 dias
			context.reply(
					LorittaReply(
							context.locale["commands.command.pay.selfAccountIsTooNew", 14] + " ${Emotes.LORI_CRYING}",
							Constants.ERROR
					)
			)
			return false
		}
		return true
	}

	private suspend fun checkIfOtherAccountIsOldEnough(context: CommandContext, target: User): Boolean {
		val epochMillis = target.timeCreated.toEpochSecond() * 1000

		if (epochMillis + Constants.ONE_WEEK_IN_MILLISECONDS > System.currentTimeMillis()) { // 7 dias
			context.reply(
					LorittaReply(
							context.locale["commands.command.pay.otherAccountIsTooNew", target.asMention, 7] + " ${Emotes.LORI_CRYING}",
							Constants.ERROR
					)
			)
			return false
		}
		return true
	}

	enum class PayStatus {
		INVALID_MONEY_STATUS,
		NOT_ENOUGH_MONEY,
		SUCCESS
	}
}