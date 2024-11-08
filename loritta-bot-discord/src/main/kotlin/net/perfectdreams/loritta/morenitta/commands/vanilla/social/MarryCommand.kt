package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.Marriage
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction
import net.perfectdreams.loritta.morenitta.utils.extensions.isEmote
import net.perfectdreams.loritta.morenitta.utils.onReactionAdd
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import net.perfectdreams.loritta.serializable.StoredMarriageMarryTransaction
import java.time.Instant

class MarryCommand(loritta: LorittaBot) : AbstractCommand(loritta, "marry", listOf("casar"), net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
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

			if (proposeTo.id == loritta.config.loritta.discord.applicationId.toString()) {
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
				if (it.emoji.isEmote("\uD83D\uDC8D") && it.member?.user?.id == proposeTo.id) {
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

					if (proposeTo.id == loritta.config.loritta.discord.applicationId.toString()) {
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

						val splitCostAsLong = splitCost.toLong()

						profile.takeSonhosAndAddToTransactionLogNested(
							splitCostAsLong,
							SonhosPaymentReason.MARRIAGE
						)

						proposeToProfile.takeSonhosAndAddToTransactionLogNested(
							splitCostAsLong,
							SonhosPaymentReason.MARRIAGE
						)

						// Cinnamon transactions log
						SimpleSonhosTransactionsLogUtils.insert(
							profile.id.value,
							Instant.now(),
							TransactionType.MARRIAGE,
							splitCostAsLong,
							StoredMarriageMarryTransaction(
								proposeToProfile.id.value
							)
						)

						SimpleSonhosTransactionsLogUtils.insert(
							proposeToProfile.id.value,
							Instant.now(),
							TransactionType.MARRIAGE,
							splitCostAsLong,
							StoredMarriageMarryTransaction(
								profile.id.value
							)
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