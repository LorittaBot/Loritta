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
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.NumberUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

class PagarCommand : AbstractCommand("pay", listOf("pagar"), CommandCategory.ECONOMY) {
	companion object {
		private val mutex = Mutex()
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.economy.pay.description"]
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
                                locale.toNewLocale()["commands.social.reputation.description"],
                                Constants.ERROR
                        )
				)
				return
			}

			val howMuch = NumberUtils.convertShortenedNumberToLong(arg1)

			if (howMuch == null) {
				context.reply(
                        LorittaReply(
                                locale["INVALID_NUMBER", arg1],
                                Constants.ERROR
                        )
				)
				return
			}

			if (1 > howMuch) {
				context.reply(
                        LorittaReply(
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
                        LorittaReply(
                                locale["PAY_InsufficientFunds", if (economySource == "global") locale["ECONOMY_NamePlural"] else economyConfig?.economyNamePlural],
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

				if (user.idLong == loritta.discordConfig.discord.clientId.toLong() && 50_000 > howMuch) {
					// If the user is transferring to Loritta, we check if the user is transferring less than 50_000 and, if yes, we won't allow it.
					context.reply(
							LorittaReply(
									context.locale["commands.economy.pay.doYouThinkImPoor"],
									Emotes.LORI_BAN_HAMMER
							)
					)
					return
				}

				val quirkyMessage = if (howMuch >= 500_000) {
					" ${context.locale.getList("commands.economy.pay.randomQuirkyRichMessages").random()}"
				} else { "" }

				val message = context.reply(
                        LorittaReply(
                                context.locale["commands.economy.pay.youAreGoingToTransfer", howMuch, user.asMention, quirkyMessage],
                                Emotes.LORI_RICH
                        ),
                        LorittaReply(
                                context.locale["commands.economy.pay.clickToAcceptTheTransaction", user.asMention, "✅"],
                                "🤝",
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
                                                        locale["PAY_TransactionComplete", user.asMention, finalMoney, if (finalMoney == 1.0) {
                                                            locale.toNewLocale()["economy.currency.name.singular"]
                                                        } else {
                                                            locale.toNewLocale()["economy.currency.name.plural"]
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
                                locale["PAY_TransactionComplete", user.asMention, howMuch, if (howMuch.toLong() == 1L) {
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

	private suspend fun checkIfSelfAccountIsOldEnough(context: CommandContext): Boolean {
		val epochMillis = context.userHandle.timeCreated.toEpochSecond() * 1000

		if (epochMillis + (Constants.ONE_WEEK_IN_MILLISECONDS * 2) > System.currentTimeMillis()) { // 14 dias
			context.reply(
                    LorittaReply(
                            context.locale["commands.economy.pay.selfAccountIsTooNew", 14] + " ${Emotes.LORI_CRYING}",
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
                            context.locale["commands.economy.pay.otherAccountIsTooNew", target.asMention, 7] + " ${Emotes.LORI_CRYING}",
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