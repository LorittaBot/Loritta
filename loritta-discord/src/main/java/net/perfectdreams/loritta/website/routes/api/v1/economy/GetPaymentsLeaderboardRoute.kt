package net.perfectdreams.loritta.website.routes.api.v1.economy

import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.serializable.DiscordUser
import net.perfectdreams.loritta.serializable.PaymentScoreboardEntry
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.utils.payments.PaymentReason
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.sum
import java.time.Instant
import java.time.ZoneOffset

class GetPaymentsLeaderboardRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/economy/payments-leaderboard/{select}/{type}/{span}") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val select = call.parameters["select"]
		val type = call.parameters["type"]
		val span = call.parameters["span"]

		if (type == "top") {
			val size = call.parameters["size"]!!.toInt()
			if (size !in 0..5)
				return

			val sum = Payments.money.sum()

			val monthStart = Instant.now()
					.atOffset(ZoneOffset.UTC).withDayOfMonth(1)
					.toInstant()
					.toEpochMilli()

			val paymentReason = when (select) {
				"bundles" -> PaymentReason.SONHOS_BUNDLE
				"premium" -> PaymentReason.DONATION
				else -> throw RuntimeException("Invalid select $select")
			}

			val contents = loritta.newSuspendedTransaction {
				Payments.slice(Payments.userId, sum)
						.let {
							if (span == "lifetime") {
								it.select { Payments.paidAt.isNotNull() and (Payments.reason eq paymentReason) }
							} else {
								it.select {
									Payments.paidAt.isNotNull() and
											(Payments.reason eq paymentReason) and
											(Payments.paidAt greaterEq monthStart)

								}
							}
						}
						.orderBy(sum, SortOrder.DESC)
						.groupBy(Payments.userId)
						.limit(size)
						.toList()
			}

			val users = contents.mapNotNull {
				val userInfo = lorittaShards.retrieveUserInfoById(it[Payments.userId])

				if (userInfo != null) {
					PaymentScoreboardEntry(
							it[sum]!!.toDouble(),
							DiscordUser(
									userInfo.id,
									userInfo.name,
									userInfo.discriminator,
									userInfo.effectiveAvatarUrl
							)
					)
				} else null
			}

			call.respondJson(Json.encodeToJsonElement(ListSerializer(PaymentScoreboardEntry.serializer()), users))
		}
	}
}