package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Marriage
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionAdd
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.PaymentUtils
import net.perfectdreams.loritta.utils.SonhosPaymentReason

class MarryCommand : AbstractCommand("marry", listOf("casar"), CommandCategory.SOCIAL) {
	companion object {
		val MARRIAGE_COST = 15_000
	}

	override fun getDescriptionKey() = LocaleKeyData("commands.command.marry.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.marry.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val proposeTo = context.getUserAt(0)

		if (proposeTo != null) {
			val proposeToProfile = loritta.getOrCreateLorittaProfile(proposeTo.id)
			val marriage = loritta.newSuspendedTransaction { context.lorittaUser.profile.marriage }
			val proposeMarriage = loritta.newSuspendedTransaction { proposeToProfile.marriage }

			val splitCost = MARRIAGE_COST / 2

			if (proposeTo.id == context.userHandle.id) {
				context.reply(
                        LorittaReply(
                                locale["commands.command.marry.cantMarryYourself"],
                                Constants.ERROR
                        )
				)
				return
			}

			if (proposeTo.id == loritta.discordConfig.discord.clientId) {
				context.reply(
                        LorittaReply(
                                locale["commands.command.marry.marryLoritta"],
                                "<:smol_lori_putassa:395010059157110785>"
                        )
				)
				return
			}

			if (marriage != null) {
				// Já está casado!
				context.reply(
                        LorittaReply(
                                locale["commands.command.marry.alreadyMarried", context.config.commandPrefix],
                                Constants.ERROR
                        )
				)
				return
			}

			if (proposeMarriage != null) {
				// Já está casado!
				context.reply(
                        LorittaReply(
                                locale["commands.command.marry.alreadyMarriedOther", proposeTo.asMention],
                                Constants.ERROR
                        )
				)
				return
			}

			if (splitCost > context.lorittaUser.profile.money) {
				// Não tem dinheiro suficiente!
				val diff = splitCost - context.lorittaUser.profile.money
				context.reply(
                        LorittaReply(
                                locale["commands.command.marry.insufficientFunds", diff],
                                Constants.ERROR
                        )
				)
				return
			}

			if (splitCost > proposeToProfile.money) {
				// Não tem dinheiro suficiente!
				val diff = splitCost - proposeToProfile.money
				context.reply(
                        LorittaReply(
                                locale["commands.command.marry.insufficientFundsOther", proposeTo.asMention, diff],
                                Constants.ERROR
                        )
				)
				return
			}

			// Pedido enviado!
			val replies = listOf(
                    LorittaReply(
                            proposeTo.asMention + " Você recebeu uma proposta de casamento de " + context.userHandle.asMention + "!",
                            "\uD83D\uDC8D"
                    ),
                    LorittaReply(
                            "Para aceitar, clique no \uD83D\uDC8D! Mas lembrando, o custo de um casamento é **15000 Sonhos** (7500 para cada usuário), e **250 Sonhos** todos os dias!",
                            "\uD83D\uDCB5"
                    )
			)

			val response = replies.joinToString("\n", transform = { it.build() })
			val message = context.sendMessage(response)

			message.onReactionAdd(context) {
				if (it.reactionEmote.isEmote("\uD83D\uDC8D") && it.member?.user?.id == proposeTo.id) {
					message.delete().queue()

					val profile = loritta.getOrCreateLorittaProfile(context.userHandle.id)
					val proposeToProfile = loritta.getOrCreateLorittaProfile(proposeTo.id)
					val marriage = loritta.newSuspendedTransaction { context.lorittaUser.profile.marriage }
					val proposeMarriage = loritta.newSuspendedTransaction { context.lorittaUser.profile.marriage }

					if (proposeTo.id == context.userHandle.id) {
						context.reply(
                                LorittaReply(
                                        locale["commands.command.marry.cantMarryYourself"],
                                        Constants.ERROR
                                )
						)
						return@onReactionAdd
					}

					if (proposeTo.id == loritta.discordConfig.discord.clientId) {
						context.reply(
                                LorittaReply(
                                        locale["commands.command.marry.loritta"],
                                        "<:smol_lori_putassa:395010059157110785>"
                                )
						)
						return@onReactionAdd
					}

					if (marriage != null) {
						// Não tem dinheiro suficiente!
						context.reply(
                                LorittaReply(
                                        locale["commands.command.marry.alreadyMarried"],
                                        Constants.ERROR
                                )
						)
						return@onReactionAdd
					}

					if (proposeMarriage != null) {
						// Já está casado!
						context.reply(
                                LorittaReply(
                                        locale["commands.command.marry.alreadyMarriedOther"],
                                        Constants.ERROR
                                )
						)
						return@onReactionAdd
					}

					if (splitCost > profile.money) {
						// Não tem dinheiro suficiente!
						val diff = splitCost - profile.money
						context.reply(
                                LorittaReply(
                                        locale["commands.command.marry.insufficientFunds", diff],
                                        Constants.ERROR
                                )
						)
						return@onReactionAdd
					}

					if (splitCost > proposeToProfile.money) {
						// Não tem dinheiro suficiente!
						val diff = splitCost - proposeToProfile.money
						context.reply(
                                LorittaReply(
                                        locale["commands.command.marry.insufficientFundsOther", proposeTo.asMention, diff],
                                        Constants.ERROR
                                )
						)
						return@onReactionAdd
					}

					// Okay, tudo certo, vamos lá!
					loritta.newSuspendedTransaction {
						val newMarriage = Marriage.new {
							user1 = context.userHandle.idLong
							user2 = proposeTo.idLong
							marriedSince = System.currentTimeMillis()
						}
						profile.marriage = newMarriage
						proposeToProfile.marriage = newMarriage

						profile.takeSonhosNested(splitCost.toLong())
						proposeToProfile.takeSonhosNested(splitCost.toLong())

						PaymentUtils.addToTransactionLogNested(
								splitCost.toLong(),
								SonhosPaymentReason.MARRIAGE,
								givenBy = profile.id.value
						)

						PaymentUtils.addToTransactionLogNested(
								splitCost.toLong(),
								SonhosPaymentReason.MARRIAGE,
								givenBy = proposeToProfile.id.value
						)
					}

					context.reply(
                            LorittaReply(
                                    "Vocês se casaram! Felicidades para vocês dois!",
                                    "❤"
                            )
					)
				}
			}

			message.addReaction("\uD83D\uDC8D").queue()
		} else {
			context.explain()
		}
	}
}