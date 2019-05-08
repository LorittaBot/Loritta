package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Marriage
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionAdd
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.jetbrains.exposed.sql.transactions.transaction

class MarryCommand : AbstractCommand("marry", listOf("casar"), CommandCategory.SOCIAL) {
	companion object {
		val MARRIAGE_COST = 15000
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["MARRY_Description"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val proposeTo = context.getUserAt(0)

		if (proposeTo != null) {
			val proposeToProfile = loritta.getOrCreateLorittaProfile(proposeTo.id)
			val marriage = transaction(Databases.loritta) { context.lorittaUser.profile.marriage }
			val proposeMarriage = transaction(Databases.loritta) { proposeToProfile.marriage }

			val splitCost = MARRIAGE_COST / 2

			if (proposeTo.id == context.userHandle.id) {
				context.reply(
						LoriReply(
								locale["MARRY_CantMarryYourself"],
								Constants.ERROR
						)
				)
				return
			}

			if (proposeTo.id == loritta.discordConfig.discord.clientId) {
				context.reply(
						LoriReply(
								locale["MARRY_Loritta"],
								"<:smol_lori_putassa:395010059157110785>"
						)
				)
				return
			}

			if (marriage != null) {
				// Já está casado!
				context.reply(
						LoriReply(
								locale["MARRY_AlreadyMarried", context.config.commandPrefix],
								Constants.ERROR
						)
				)
				return
			}

			if (proposeMarriage != null) {
				// Já está casado!
				context.reply(
						LoriReply(
								locale["MARRY_AlreadyMarriedOther"],
								Constants.ERROR
						)
				)
				return
			}

			if (splitCost > context.lorittaUser.profile.money) {
				// Não tem dinheiro suficiente!
				val diff = splitCost - context.lorittaUser.profile.money
				context.reply(
						LoriReply(
								locale["MARRY_InsufficientFunds", diff],
								Constants.ERROR
						)
				)
				return
			}

			if (splitCost > proposeToProfile.money) {
				// Não tem dinheiro suficiente!
				val diff = splitCost - proposeToProfile.money
				context.reply(
						LoriReply(
								locale["MARRY_InsufficientFundsOther", proposeTo.asMention, diff],
								Constants.ERROR
						)
				)
				return
			}

			// Pedido enviado!
			val replies = listOf(
					LoriReply(
							proposeTo.asMention + " Você recebeu uma proposta de casamento de " + context.userHandle.asMention + "!",
							"\uD83D\uDC8D"
					),
					LoriReply(
							"Para aceitar, clique no \uD83D\uDC8D! Mas lembrando, o custo de um casamento é **10000 Sonhos**, e **200 Sonhos** todos os dias!",
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
					val marriage = transaction(Databases.loritta) { context.lorittaUser.profile.marriage }
					val proposeMarriage = transaction(Databases.loritta) { context.lorittaUser.profile.marriage }

					if (proposeTo.id == context.userHandle.id) {
						context.reply(
								LoriReply(
										locale["MARRY_CantMarryYourself"],
										Constants.ERROR
								)
						)
						return@onReactionAdd
					}

					if (proposeTo.id == loritta.discordConfig.discord.clientId) {
						context.reply(
								LoriReply(
										locale["MARRY_Loritta"],
										"<:smol_lori_putassa:395010059157110785>"
								)
						)
						return@onReactionAdd
					}

					if (marriage != null) {
						// Não tem dinheiro suficiente!
						context.reply(
								LoriReply(
										locale["MARRY_AlreadyMarried"],
										Constants.ERROR
								)
						)
						return@onReactionAdd
					}

					if (proposeMarriage != null) {
						// Já está casado!
						context.reply(
								LoriReply(
										locale["MARRY_AlreadyMarriedOther"],
										Constants.ERROR
								)
						)
						return@onReactionAdd
					}

					if (splitCost > profile.money) {
						// Não tem dinheiro suficiente!
						val diff = splitCost - profile.money
						context.reply(
								LoriReply(
										locale["MARRY_InsufficientFunds", diff],
										Constants.ERROR
								)
						)
						return@onReactionAdd
					}

					if (splitCost > proposeToProfile.money) {
						// Não tem dinheiro suficiente!
						val diff = splitCost - proposeToProfile.money
						context.reply(
								LoriReply(
										locale["MARRY_InsufficientFundsOther", proposeTo.asMention, diff],
										Constants.ERROR
								)
						)
						return@onReactionAdd
					}

					// Okay, tudo certo, vamos lá!
					transaction(Databases.loritta) {
						val newMarriage = Marriage.new {
							user1 = context.userHandle.idLong
							user2 = proposeTo.idLong
							marriedSince = System.currentTimeMillis()
						}
						profile.marriage = newMarriage
						proposeToProfile.marriage = newMarriage
						profile.money -= splitCost
						proposeToProfile.money -= splitCost
					}

					context.reply(
							LoriReply(
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