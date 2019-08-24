package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.threads.RaffleThread
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.FeatureFlags
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.Executors

class LoraffleCommand : AbstractCommand("loraffle", listOf("rifa", "raffle", "lorifa"), CommandCategory.ECONOMY) {
	companion object {
		val coroutineExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
		const val MAX_TICKETS_BY_USER_PER_ROUND = 1000
		const val MAX_TICKET_ABUSE_THRESHOLD_LORI = 5_000
		const val LORI_ID = 297153970613387264L
		const val MAX_TICKET_ABUSE_THRESHOLD_PANTUFA = 10_000
		const val PANTUFA_ID = 390927821997998081L
		const val MAX_TICKET_ABUSE_THRESHOLD_GABI = 15_000
		const val GABI_ID = 481901252007952385L
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["RAFFLE_Description"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val arg0 = context.args.getOrNull(0)

		if (arg0 == "clear" && loritta.config.isOwner(context.userHandle.id)) {
			context.reply(
					LoriReply(
							"Limpando ${RaffleThread.userIds.size}..."
					)
			)
			RaffleThread.userIds.clear()
			context.reply(
					LoriReply(
							"Limpo! ${RaffleThread.userIds.size}"
					)
			)
			return
		}

		if (arg0 == "comprar" || arg0 == "buy") {
			val quantity = Math.max(context.args.getOrNull(1)?.toIntOrNull() ?: 1, 1)

			if (quantity > MAX_TICKETS_BY_USER_PER_ROUND) {
				context.reply(
						LoriReply(
								"Você só pode apostar no máximo $MAX_TICKETS_BY_USER_PER_ROUND tickets por rodada!",
								Constants.ERROR
						)
				)
				return
			}

			val currentUserTicketQuantity = RaffleThread.userIds.count { it.first == context.userHandle.id }
			if (RaffleThread.userIds.count { it.first == context.userHandle.id } + quantity > MAX_TICKETS_BY_USER_PER_ROUND) {
				if (currentUserTicketQuantity == MAX_TICKETS_BY_USER_PER_ROUND) {
					context.reply(
							LoriReply(
									"Você já tem tickets demais! Guarde um pouco do seu dinheiro para a próxima rodada!",
									Constants.ERROR
							)
					)
				} else {
					context.reply(
							LoriReply(
									"Você não pode apostar tantos tickets assim! Você pode apostar, no máximo, mais ${MAX_TICKETS_BY_USER_PER_ROUND - currentUserTicketQuantity} tickets!",
									Constants.ERROR
							)
					)
				}
				return
			}

			val requiredCount = quantity.toLong() * 250
			RaffleThread.logger.info("${context.userHandle.id} irá comprar $quantity tickets por ${requiredCount}!")

			GlobalScope.launch(coroutineExecutor) {
				val lorittaProfile = loritta.getOrCreateLorittaProfile(context.userHandle.id)

				if (lorittaProfile.money >= requiredCount) {
					transaction(Databases.loritta) {
						lorittaProfile.money -= requiredCount
					}

					for (i in 0 until quantity) {
						RaffleThread.userIds.add(Pair(context.userHandle.id, context.config.localeId))
					}

					RaffleThread.logger.info("${context.userHandle.id} comprou $quantity tickets por ${requiredCount}! (Antes ele possuia ${lorittaProfile.money + requiredCount}) sonhos!")

					if (FeatureFlags.isEnabled(FeatureFlags.BOTS_CAN_HAVE_FUN_IN_THE_RAFFLE_TOO)) {
						val safeSize = RaffleThread.userIds.filter {
							it.first !in arrayOf(LORI_ID.toString(), PANTUFA_ID.toString(), GABI_ID.toString())
						}.size

						val loriAlreadyDidIt = RaffleThread.userIds.any { it.first == LORI_ID.toString() }
						val pantufaAlreadyDidIt = RaffleThread.userIds.any { it.first == PANTUFA_ID.toString() }
						val gabiAlreadyDidIt = RaffleThread.userIds.any { it.first == GABI_ID.toString() }

						when {
							safeSize >= MAX_TICKET_ABUSE_THRESHOLD_LORI && !loriAlreadyDidIt -> {
								logger.info { "Sorry, we don't do that around here. Ticket threshold $safeSize > $MAX_TICKET_ABUSE_THRESHOLD_LORI... Loritta will take care of this. ^-^" }

								for (i in 0 until MAX_TICKETS_BY_USER_PER_ROUND) {
									RaffleThread.userIds.add(Pair(LORI_ID.toString(), "default"))
								}
							}
							safeSize >= MAX_TICKET_ABUSE_THRESHOLD_PANTUFA && !pantufaAlreadyDidIt -> {
								logger.info { "Sorry, we don't do that around here. Ticket threshold $safeSize > $MAX_TICKET_ABUSE_THRESHOLD_PANTUFA... Pantufa will take care of this. ^-^" }

								for (i in 0 until MAX_TICKETS_BY_USER_PER_ROUND) {
									RaffleThread.userIds.add(Pair(PANTUFA_ID.toString(), "default"))
								}
							}
							safeSize >= MAX_TICKET_ABUSE_THRESHOLD_GABI && !gabiAlreadyDidIt -> {
								logger.info { "Sorry, we don't do that around here. Ticket threshold $safeSize > $MAX_TICKET_ABUSE_THRESHOLD_GABI... Gabriela will take care of this. ^-^" }

								for (i in 0 until MAX_TICKETS_BY_USER_PER_ROUND) {
									RaffleThread.userIds.add(Pair(GABI_ID.toString(), "default"))
								}
							}
						}
					}

					loritta.raffleThread.save()

					context.reply(
							LoriReply(
									context.legacyLocale["RAFFLE_YouBoughtAnTicket", quantity, if (quantity == 1) "" else "s", requiredCount],
									"\uD83C\uDFAB"
							),
							LoriReply(
									context.legacyLocale["RAFFLE_WantMoreChances", context.config.commandPrefix],
									mentionUser = false
							)
					)
				} else {
					context.reply(
							LoriReply(
									context.legacyLocale["RAFFLE_NotEnoughMoney", requiredCount - lorittaProfile.money, quantity, if (quantity == 1) "" else "s"],
									Constants.ERROR
							)
					)
				}
			}
			return
		}

		val cal = Calendar.getInstance()
		cal.timeInMillis = RaffleThread.started + 3600000

		val lastWinner = if (RaffleThread.lastWinnerId != null) {
			lorittaShards.getUserById(RaffleThread.lastWinnerId)
		} else {
			null
		}

		var nameAndDiscriminator = if (lastWinner != null) {
			lastWinner.name + "#" + lastWinner.discriminator
		} else {
			"\uD83E\uDD37"
		}

		context.reply(
				LoriReply(
						"**Lorifa**",
						"<:loritta:331179879582269451>"
				),
				LoriReply(
						context.legacyLocale["RAFFLE_CurrentPrize", (RaffleThread.userIds.size * 250).toString()],
						"<:starstruck:540988091117076481>",
						mentionUser = false
				),
				LoriReply(
						context.legacyLocale["RAFFLE_BoughtTickets", RaffleThread.userIds.size],
						"\uD83C\uDFAB",
						mentionUser = false
				),
				LoriReply(
						context.legacyLocale["RAFFLE_UsersParticipating", RaffleThread.userIds.distinctBy { it.first }.size],
						"\uD83D\uDC65",
						mentionUser = false
				),
				LoriReply(
						context.legacyLocale["RAFFLE_LastWinner", "${nameAndDiscriminator.stripCodeMarks()} (${lastWinner?.id})", RaffleThread.lastWinnerPrize],
						"\uD83D\uDE0E",
						mentionUser = false
				),
				LoriReply(
						context.legacyLocale["RAFFLE_ResultsIn", DateUtils.formatDateDiff(Calendar.getInstance(), cal, locale)],
						prefix = "\uD83D\uDD52",
						mentionUser = false
				),
				LoriReply(
						context.legacyLocale["RAFFLE_BuyAnTicketFor", context.config.commandPrefix],
						prefix = "\uD83D\uDCB5",
						mentionUser = false
				)
		)
	}
}