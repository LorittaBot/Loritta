package net.perfectdreams.loritta.morenitta.website.routes.api.v1.guild

import com.github.salomonbrys.kotson.jsonObject
import io.ktor.server.application.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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

class GetGuildUserEmojiFightVictoriesRoute(loritta: LorittaBot) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/guilds/{guildId}/users/{userId}/emojifight/victories") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val guildId = call.parameters["guildId"] ?: return
		val userId = call.parameters["userId"]?.toLongOrNull() ?: return

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
			EmojiFightMatches.guild eq guild.idLong and (EmojiFightParticipants.user eq userId)
		}

		if (beforeDate != null)
			query = query and (EmojiFightMatches.finishedAt greaterEq beforeDate)

		if (afterDate != null)
			query = query and (EmojiFightMatches.finishedAt greaterEq afterDate)

		if (minimumEntryPrice != null)
			query = query and (EmojiFightMatchmakingResults.entryPrice greaterEq minimumEntryPrice)

		val winCount = EmojiFightParticipants.user.count()
		val result = loritta.transaction {
			EmojiFightMatchmakingResults
				.innerJoin(EmojiFightMatches)
				.innerJoin(EmojiFightParticipants)
				.select(EmojiFightParticipants.user, winCount)
				.where(query)
				.groupBy(EmojiFightParticipants.user)
				.orderBy(winCount, SortOrder.DESC)
				.offset(offset).limit(limit)
				.toList()
				.firstOrNull()
		}

		if (result != null) {
			call.respondJson(
				buildJsonObject {
					put("id", userId.toString())
					put("victories", result[winCount])
				}
			)
		} else {
			call.respondJson(
				buildJsonObject {
					put("id", userId.toString())
					put("victories", 0)
				}
			)
		}
	}
}