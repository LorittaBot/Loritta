package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.threads.RaffleThread
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import java.util.*

class LoraffleCommand : AbstractCommand("loraffle", listOf("rifa", "raffle", "lorifa"), CommandCategory.ECONOMY) {
	companion object {
		const val MAX_TICKETS_BY_USER_PER_ROUND = 100_000
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["RAFFLE_Description"]
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val arg0 = context.args.getOrNull(0)

		if (arg0 == "clear" && loritta.config.isOwner(context.userHandle.id)) {
			context.reply(
                    LorittaReply(
                            "Limpando ${RaffleThread.userIds.size}..."
                    )
			)
			RaffleThread.userIds.clear()
			context.reply(
                    LorittaReply(
                            "Limpo! ${RaffleThread.userIds.size}"
                    )
			)
			return
		}

		val shard = loritta.config.clusters.first { it.id == 1L }

		if (arg0 == "comprar" || arg0 == "buy") {
			val quantity = Math.max(context.args.getOrNull(1)?.toIntOrNull() ?: 1, 1)

			val (canGetDaily, tomorrow) = context.lorittaUser.profile.canGetDaily()

			if (canGetDaily) { // Nós apenas queremos permitir que a pessoa aposte na rifa caso já tenha pegado sonhos alguma vez hoje
				context.reply(
                        LorittaReply(
                                "Parece que você ainda não pegou o seu daily, você só pode apostar na rifa após ter pegado o seu daily de hoje. Pegue agora mesmo! ${loritta.instanceConfig.loritta.website.url}daily",
                                Constants.ERROR
                        )
				)
				return
			}

			if (quantity > MAX_TICKETS_BY_USER_PER_ROUND) {
				context.reply(
                        LorittaReply(
                                "Você só pode apostar no máximo $MAX_TICKETS_BY_USER_PER_ROUND tickets por rodada!",
                                Constants.ERROR
                        )
				)
				return
			}

			val body = HttpRequest.post("https://${shard.getUrl()}/api/v1/loritta/raffle")
					.userAgent(loritta.lorittaCluster.getUserAgent())
					.header("Authorization", loritta.lorittaInternalApiKey.name)
					.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
					.readTimeout(loritta.config.loritta.clusterReadTimeout)
					.send(
							gson.toJson(
									jsonObject(
											"userId" to context.userHandle.id,
											"quantity" to quantity,
											"localeId" to context.config.localeId
									)
							)
					)
					.body()

			val json = JsonParser.parseString(body)

			val status = BuyRaffleTicketStatus.valueOf(json["status"].string)

			if (status == BuyRaffleTicketStatus.THRESHOLD_EXCEEDED) {
				context.reply(
                        LorittaReply(
                                "Você já tem tickets demais! Guarde um pouco do seu dinheiro para a próxima rodada!",
                                Constants.ERROR
                        )
				)
				return
			}

			if (status == BuyRaffleTicketStatus.TOO_MANY_TICKETS) {
				context.reply(
                        LorittaReply(
                                "Você não pode apostar tantos tickets assim! Você pode apostar, no máximo, mais ${MAX_TICKETS_BY_USER_PER_ROUND - json["ticketCount"].int} tickets!",
                                Constants.ERROR
                        )
				)
				return
			}

			if (status == BuyRaffleTicketStatus.NOT_ENOUGH_MONEY) {
				context.reply(
                        LorittaReply(
                                context.legacyLocale["RAFFLE_NotEnoughMoney", json["canOnlyPay"].int, quantity, if (quantity == 1) "" else "s"],
                                Constants.ERROR
                        )
				)
				return
			}

			context.reply(
                    LorittaReply(
                            context.legacyLocale["RAFFLE_YouBoughtAnTicket", quantity, if (quantity == 1) "" else "s", quantity.toLong() * 250],
                            "\uD83C\uDFAB"
                    ),
                    LorittaReply(
                            context.legacyLocale["RAFFLE_WantMoreChances", context.config.commandPrefix],
                            mentionUser = false
                    )
			)
			return
		}

		val body = HttpRequest.get("https://${shard.getUrl()}/api/v1/loritta/raffle")
				.userAgent(loritta.lorittaCluster.getUserAgent())
				.header("Authorization", loritta.lorittaInternalApiKey.name)
				.connectTimeout(loritta.config.loritta.clusterConnectionTimeout)
				.readTimeout(loritta.config.loritta.clusterReadTimeout)
				.body()

		val json = JsonParser.parseString(body)

		val lastWinnerId = json["lastWinnerId"].nullString
				?.toLongOrNull()
		val currentTickets = json["currentTickets"].int
		val usersParticipating = json["usersParticipating"].int
		val started = json["started"].long
		val lastWinnerPrize = json["lastWinnerPrize"].long

		val cal = Calendar.getInstance()
		cal.timeInMillis = started + 3600000

		val lastWinner = if (lastWinnerId != null) {
			lorittaShards.retrieveUserInfoById(lastWinnerId.toLong())
		} else {
			null
		}

		val nameAndDiscriminator = if (lastWinner != null) {
			(lastWinner.name + "#" + lastWinner.discriminator).let {
				if (MiscUtils.hasInvite(it))
					"¯\\_(ツ)_/¯"
				else
					it
			}
		} else {
			"\uD83E\uDD37"
		}.stripCodeMarks()

		context.reply(
                LorittaReply(
                        "**Lorifa**",
                        "<:loritta:331179879582269451>"
                ),
                LorittaReply(
                        context.legacyLocale["RAFFLE_CurrentPrize", (currentTickets * 250).toString()],
                        "<:starstruck:540988091117076481>",
                        mentionUser = false
                ),
                LorittaReply(
                        context.legacyLocale["RAFFLE_BoughtTickets", currentTickets],
                        "\uD83C\uDFAB",
                        mentionUser = false
                ),
                LorittaReply(
                        context.legacyLocale["RAFFLE_UsersParticipating", usersParticipating],
                        "\uD83D\uDC65",
                        mentionUser = false
                ),
                LorittaReply(
                        context.legacyLocale["RAFFLE_LastWinner", "$nameAndDiscriminator (${lastWinner?.id})", lastWinnerPrize],
                        "\uD83D\uDE0E",
                        mentionUser = false
                ),
                LorittaReply(
                        context.legacyLocale["RAFFLE_ResultsIn", DateUtils.formatDateDiff(Calendar.getInstance(), cal, locale)],
                        prefix = "\uD83D\uDD52",
                        mentionUser = false
                ),
                LorittaReply(
                        context.legacyLocale["RAFFLE_BuyAnTicketFor", context.config.commandPrefix],
                        prefix = "\uD83D\uDCB5",
                        mentionUser = false
                )
		)
	}

	enum class BuyRaffleTicketStatus {
		THRESHOLD_EXCEEDED,
		TOO_MANY_TICKETS,
		NOT_ENOUGH_MONEY,
		SUCCESS
	}
}