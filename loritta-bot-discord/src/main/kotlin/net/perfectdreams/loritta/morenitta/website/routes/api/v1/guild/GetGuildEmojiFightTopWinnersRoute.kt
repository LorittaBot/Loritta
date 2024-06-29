package net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.jsonObject
import io.ktor.server.application.*
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightParticipants
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import java.time.Instant

class GetGuildEmojiFightTopWinnersRoute(loritta: LorittaBot) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/guilds/{guildId}/emojifight/top-winners") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val guildId = call.parameters["guildId"] ?: return

		val guild = loritta.lorittaShards.getGuildById(guildId)

		if (guild == null) {
			call.respondJson(
				jsonObject()
			)
			return
		}

		val offset = call.request.queryParameters["offset"] ?.toLongOrNull() ?: 0
		val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceAtMost(100) ?: 10

		val beforeDate = call.request.queryParameters["beforeDate"]?.let { Instant.parse(it) }
		val afterDate = call.request.queryParameters["afterDate"]?.let { Instant.parse(it) }
		val minimumEntryPrice = call.request.queryParameters["minimumEntryPrice"]?.toLongOrNull()

		var query = Op.build {
			EmojiFightMatches.guild eq guild.idLong
		}

		if (beforeDate != null)
			query = query and (EmojiFightMatches.finishedAt greaterEq beforeDate)

		if (afterDate != null)
			query = query and (EmojiFightMatches.finishedAt greaterEq afterDate)

		if (minimumEntryPrice != null)
			query = query and (EmojiFightMatchmakingResults.entryPrice greaterEq minimumEntryPrice)

		val winCount = EmojiFightParticipants.user.count()
		val start = kotlinx.datetime.Clock.System.now()
		val results = loritta.transaction {
			val total = EmojiFightMatchmakingResults
				.innerJoin(EmojiFightMatches)
				.innerJoin(EmojiFightParticipants)
				.select(EmojiFightParticipants.user, winCount)
				.where(query)
				.groupBy(EmojiFightParticipants.user)
				.orderBy(winCount, SortOrder.DESC)
				.count()

			if (offset > total)
				return@transaction listOf()

			EmojiFightMatchmakingResults
				.innerJoin(EmojiFightMatches)
				.innerJoin(EmojiFightParticipants)
				.select(EmojiFightParticipants.user, winCount)
				.where(query)
				.groupBy(EmojiFightParticipants.user)
				.orderBy(winCount, SortOrder.DESC)
				.limit(limit, offset)
				.toList()
		}

		call.respondJson(
			buildJsonObject {
				putJsonArray("results") {
					for (result in results) {
						addJsonObject {
							put("id", result[EmojiFightParticipants.user].value)
							put("victories", result[winCount])
						}
					}
				}
			}
		)
	}
}