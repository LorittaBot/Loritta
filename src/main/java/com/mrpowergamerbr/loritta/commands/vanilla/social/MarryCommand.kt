package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mongodb.client.model.*
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onReactionAdd
import org.bson.Document

class MarryCommand : AbstractCommand("marry", listOf("casar"), CommandCategory.SOCIAL) {
	companion object {
		val MARRIAGE_COST = 15000
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["MARRY_Description"]
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val proposeTo = context.getUserAt(0)

		if (proposeTo != null) {
			val proposeToProfile = loritta.getLorittaProfileForUser(proposeTo.id)
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

			if (proposeTo.id == Loritta.config.clientId) {
				context.reply(
						LoriReply(
								locale["MARRY_Loritta"],
								"<:smol_lori_putassa:395010059157110785>"
						)
				)
				return
			}

			if (context.lorittaUser.profile.marriedWith != null) {
				// Não tem dinheiro suficiente!
				context.reply(
						LoriReply(
								locale["MARRY_AlreadyMarried"],
								Constants.ERROR
						)
				)
				return
			}

			if (proposeToProfile.marriedWith != null) {
				// Já está casado!
				context.reply(
						LoriReply(
								locale["MARRY_AlreadyMarriedOther"],
								Constants.ERROR
						)
				)
				return
			}

			if (splitCost > context.lorittaUser.profile.dreams) {
				// Não tem dinheiro suficiente!
				val diff = splitCost - context.lorittaUser.profile.dreams
				context.reply(
						LoriReply(
								locale["MARRY_InsufficientFunds", diff],
								Constants.ERROR
						)
				)
				return
			}

			if (splitCost > proposeToProfile.dreams) {
				// Não tem dinheiro suficiente!
				val diff = splitCost - proposeToProfile.dreams
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
				if (it.reactionEmote.name == "\uD83D\uDC8D" && it.member.user.id == proposeTo.id) {
					message.delete().complete()

					val profile = loritta.getLorittaProfileForUser(context.userHandle.id)
					val proposeToProfile = loritta.getLorittaProfileForUser(proposeTo.id)

					if (proposeTo.id == context.userHandle.id) {
						context.reply(
								LoriReply(
										locale["MARRY_CantMarryYourself"],
										Constants.ERROR
								)
						)
						return@onReactionAdd
					}

					if (proposeTo.id == Loritta.config.clientId) {
						context.reply(
								LoriReply(
										locale["MARRY_Loritta"],
										"<:smol_lori_putassa:395010059157110785>"
								)
						)
						return@onReactionAdd
					}

					if (profile.marriedWith != null) {
						// Não tem dinheiro suficiente!
						context.reply(
								LoriReply(
										locale["MARRY_AlreadyMarried"],
										Constants.ERROR
								)
						)
						return@onReactionAdd
					}

					if (proposeToProfile.marriedWith != null) {
						// Já está casado!
						context.reply(
								LoriReply(
										locale["MARRY_AlreadyMarriedOther"],
										Constants.ERROR
								)
						)
						return@onReactionAdd
					}

					if (splitCost > profile.dreams) {
						// Não tem dinheiro suficiente!
						val diff = splitCost - profile.dreams
						context.reply(
								LoriReply(
										locale["MARRY_InsufficientFunds", diff],
										Constants.ERROR
								)
						)
						return@onReactionAdd
					}

					if (splitCost > proposeToProfile.dreams) {
						// Não tem dinheiro suficiente!
						val diff = splitCost - proposeToProfile.dreams
						context.reply(
								LoriReply(
										locale["MARRY_InsufficientFundsOther", proposeTo.asMention, diff],
										Constants.ERROR
								)
						)
						return@onReactionAdd
					}

					val marriedAt = System.currentTimeMillis()

					// Okay, tudo certo, vamos lá!
					val bulk = listOf<WriteModel<Document>>(
							UpdateManyModel<Document>( // Retirar os sonhos dos usuários
									Filters.`in`("_id", listOf(proposeTo.id, context.userHandle.id)),
									Updates.inc(
											"dreams",
											-splitCost
									)
							),
							UpdateOneModel<Document>(
									Filters.eq("_id", context.userHandle.id),
									Updates.combine(
											Updates.set("marriedWith", proposeTo.id),
											Updates.set("marriedAt", marriedAt)
									)
							),
							UpdateOneModel<Document>(
									Filters.eq("_id", proposeTo.id),
									Updates.combine(
											Updates.set("marriedWith", context.userHandle.id),
											Updates.set("marriedAt", marriedAt)
									)
							)
					)

					loritta.mongo.getDatabase(Loritta.config.databaseName).getCollection("users").bulkWrite(
							bulk
					)

					context.reply(
							LoriReply(
									"Vocês se casaram! Felicidades para vocês dois!",
									"❤"
							)
					)
				}
			}

			message.addReaction("\uD83D\uDC8D").complete()
		} else {
			context.explain()
		}
	}
}