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
		val marriageCost = 40000
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["MARRY_Description"]
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val proposeTo = context.getUserAt(0)

		if (proposeTo != null) {
			val proposeToProfile = loritta.getLorittaProfileForUser(proposeTo.id)
			val splitCost = marriageCost / 2

			if (context.lorittaUser.profile.marriedWith != null) {
				// Não tem dinheiro suficiente!
				context.reply(
						LoriReply(
								"Você já está casado com outra pessoa! Use `+divorciar` antes de se casar com outra pessoa!",
								Constants.ERROR
						)
				)
				return
			}
			if (splitCost > context.lorittaUser.profile.dreams) {
				// Não tem dinheiro suficiente!
				context.reply(
						LoriReply(
								"Você não tem dinheiro suficiente para casar!",
								Constants.ERROR
						)
				)
				return
			}
			if (proposeToProfile.marriedWith != null) {
				// Já está casado!
				context.reply(
						LoriReply(
								"${proposeTo.asMention} já está casada com outra pessoa!",
								Constants.ERROR
						)
				)
				return
			}
			if (splitCost > proposeToProfile.dreams) {
				// Não tem dinheiro suficiente!
				context.reply(
						LoriReply(
								"${proposeTo.asMention} não tem dinheiro suficiente para casar!",
								Constants.ERROR
						)
				)
				return
			}

			// Pedido enviado!
			val replies = listOf(
					LoriReply(
							context.userHandle.asMention + " está pedindo " + proposeTo.asMention + " em casamento!",
							"\uD83D\uDC8D"
					),
					LoriReply(
							"Para aceitar, clique no \uD83D\uDC8D!"
					)
			)

			val response = replies.joinToString("\n", transform = { it.build() })
			val message = context.sendMessage(response)

			message.onReactionAdd(context) {
				if (it.reactionEmote.name == "\uD83D\uDC8D" && it.member.user.id == proposeTo.id) {
					message.delete().complete()

					val profile = loritta.getLorittaProfileForUser(context.userHandle.id)
					val proposeToProfile = loritta.getLorittaProfileForUser(proposeTo.id)

					if (profile.marriedWith != null) {
						// Não tem dinheiro suficiente!
						context.reply(
								LoriReply(
										"Você já está casado com outra pessoa! Use `+divorciar` antes de se casar com outra pessoa!",
										Constants.ERROR
								)
						)
						return@onReactionAdd
					}
					if (splitCost > profile.dreams) {
						// Não tem dinheiro suficiente!
						context.reply(
								LoriReply(
										"Você não tem dinheiro suficiente para casar!",
										Constants.ERROR
								)
						)
						return@onReactionAdd
					}
					if (proposeToProfile.marriedWith != null) {
						// Já está casado!
						context.reply(
								LoriReply(
										"${proposeTo.asMention} já está casada com outra pessoa!",
										Constants.ERROR
								)
						)
						return@onReactionAdd
					}
					if (splitCost > proposeToProfile.dreams) {
						// Não tem dinheiro suficiente!
						context.reply(
								LoriReply(
										"${proposeTo.asMention} não tem dinheiro suficiente para casar!",
										Constants.ERROR
								)
						)
						return@onReactionAdd
					}

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
									Updates.set("marriedWith", proposeTo.id)
							),
							UpdateOneModel<Document>(
									Filters.eq("_id", proposeTo.id),
									Updates.set("marriedWith", context.userHandle.id)
							)
					)

					loritta.mongo.getDatabase(Loritta.config.databaseName).getCollection("users").bulkWrite(
							bulk
					)

					context.reply(
							LoriReply(
									"Vocês se casaram! Felicidades para vocês!"
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