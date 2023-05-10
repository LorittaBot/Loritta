package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.MiscUtils
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.RaffleType
import net.perfectdreams.loritta.morenitta.LorittaBot

class LoraffleCommand(loritta: LorittaBot) : AbstractCommand(loritta, "loraffle", listOf("rifa", "raffle", "lorifa"), net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.raffle.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.raffle.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (SonhosUtils.checkIfEconomyIsDisabled(context))
			return

		val raffleType = RaffleType.LORITTA

		val arg0 = context.args.getOrNull(0)

		val shard = loritta.config.loritta.clusters.instances.first { it.id == 1 }

		if (arg0 == "comprar" || arg0 == "buy") {
			val quantity = Math.max(context.args.getOrNull(1)?.toIntOrNull() ?: 1, 1)

			val dailyReward = AccountUtils.getUserTodayDailyReward(loritta, context.lorittaUser.profile)

			if (dailyReward == null) { // Nós apenas queremos permitir que a pessoa aposte na rifa caso já tenha pegado sonhos alguma vez hoje
				context.reply(
					LorittaReply(
						locale["commands.youNeedToGetDailyRewardBeforeDoingThisAction", context.config.commandPrefix],
						Constants.ERROR
					)
				)
				return
			}

			if (quantity > raffleType.maxTicketsByUserPerRound) {
				context.reply(
					LorittaReply(
						"Você só pode apostar no máximo ${raffleType.maxTicketsByUserPerRound} tickets por rodada!",
						Constants.ERROR
					)
				)
				return
			}

			val body = loritta.httpWithoutTimeout.post("${shard.getUrl(loritta)}/api/v1/loritta/raffle") {
				userAgent(loritta.lorittaCluster.getUserAgent(loritta))
				header("Authorization", loritta.lorittaInternalApiKey.name)

				setBody(
					TextContent(
						Json.encodeToString(
							buildJsonObject {
								put("userId", context.userHandle.idLong)
								put("quantity", quantity)
								put("localeId", context.config.localeId)
								put("invokedAt", System.currentTimeMillis())
								put("type", RaffleType.LORITTA.name)
							}
						),
						ContentType.Application.Json
					)
				)
			}.bodyAsText()

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
						"Você não pode apostar tantos tickets assim! Você pode apostar, no máximo, mais ${raffleType.maxTicketsByUserPerRound - json["ticketCount"].int} tickets!",
						Constants.ERROR
					)
				)
				return
			}

			if (status == BuyRaffleTicketStatus.NOT_ENOUGH_MONEY) {
				context.reply(
					LorittaReply(
						context.locale["commands.command.raffle.notEnoughMoney", json["canOnlyPay"].int, quantity, if (quantity == 1) "" else "s"],
						Constants.ERROR
					),
					LorittaReply(
						context.i18nContext.get(
							GACampaigns.sonhosBundlesUpsellDiscordMessage(
								"https://loritta.website/", // Hardcoded, woo
								"loraffle-legacy",
								"buy-tickets-not-enough-sonhos"
							)
						),
						prefix = Emotes.LORI_RICH.asMention,
						mentionUser = false
					)
				)
				return
			}


			if (status == BuyRaffleTicketStatus.STALE_RAFFLE_DATA) {
				context.reply(
					LorittaReply(
						"O resultado da rifa demorou tanto para sair que já começou uma nova rifa enquanto você comprava!",
						Constants.ERROR
					)
				)
				return
			}

			context.reply(
				LorittaReply(
					context.locale["commands.command.raffle.youBoughtAnTicket", quantity, if (quantity == 1) "" else "s", quantity.toLong() * raffleType.ticketPrice],
					"\uD83C\uDFAB"
				),
				LorittaReply(
					context.locale["commands.command.raffle.wantMoreChances", context.config.commandPrefix],
					mentionUser = false
				)
			)
			return
		}

		val body = loritta.httpWithoutTimeout.get("${shard.getUrl(loritta)}/api/v1/loritta/raffle") {
			userAgent(loritta.lorittaCluster.getUserAgent(loritta))
			header("Authorization", loritta.lorittaInternalApiKey.name)
		}.bodyAsText()

		println(body)

		val json = JsonParser.parseString(body)

		val lastWinnerId = json["lastWinnerId"].nullString
			?.toLongOrNull()
		val currentTickets = json["currentTickets"].int
		val usersParticipating = json["usersParticipating"].int
		val endsAt = json["endsAt"].long
		val lastWinnerPrize = json["lastWinnerPrize"].long

		val lastWinner = if (lastWinnerId != null) {
			loritta.lorittaShards.retrieveUserInfoById(lastWinnerId.toLong())
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
				context.locale["commands.command.raffle.currentPrize", (currentTickets * raffleType.ticketPrice).toString()],
				"<:starstruck:540988091117076481>",
				mentionUser = false
			),
			LorittaReply(
				context.locale["commands.command.raffle.boughtTickets", currentTickets],
				"\uD83C\uDFAB",
				mentionUser = false
			),
			LorittaReply(
				context.locale["commands.command.raffle.usersParticipating", usersParticipating],
				"\uD83D\uDC65",
				mentionUser = false
			),
			LorittaReply(
				context.locale["commands.command.raffle.lastWinner", "$nameAndDiscriminator (${lastWinner?.id})", lastWinnerPrize],
				"\uD83D\uDE0E",
				mentionUser = false
			),
			LorittaReply(
				context.locale["commands.command.raffle.resultsIn", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifference(endsAt, locale)],
				prefix = "\uD83D\uDD52",
				mentionUser = false
			),
			LorittaReply(
				context.locale["commands.command.raffle.buyAnTicketFor", context.config.commandPrefix],
				prefix = "\uD83D\uDCB5",
				mentionUser = false
			)
		)
	}

	enum class BuyRaffleTicketStatus {
		SUCCESS,
		THRESHOLD_EXCEEDED,
		TOO_MANY_TICKETS,
		NOT_ENOUGH_MONEY,
		STALE_RAFFLE_DATA
	}
}