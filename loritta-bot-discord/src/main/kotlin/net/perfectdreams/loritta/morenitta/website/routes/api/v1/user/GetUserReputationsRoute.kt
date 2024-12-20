package net.perfectdreams.loritta.morenitta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import net.perfectdreams.loritta.morenitta.dao.Reputation
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import io.ktor.server.application.*
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SortOrder

class GetUserReputationsRoute(loritta: LorittaBot) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/{userId}/reputation") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val receiver = call.parameters["userId"] ?: return

		val reputations = loritta.newSuspendedTransaction {
			Reputation.find { Reputations.receivedById eq receiver.toLong() }
					.orderBy(Reputations.receivedAt to SortOrder.DESC)
					.toList()
		}

		val map = reputations.groupingBy { it.givenById }.eachCount()
				.entries
				.sortedByDescending { it.value }

		var idx = 0

		val rankedUsers = jsonArray()

		for ((userId, count) in map) {
			if (idx == 5) break
			KotlinLogging.logger {}.info { "GetUserReputationsRoute#retrieveUserInfoById - UserId: ${userId}" }
			val userInfo = loritta.lorittaShards.retrieveUserInfoById(userId) ?: continue
			rankedUsers.add(
					jsonObject(
							"count" to count,
							"user" to WebsiteUtils.transformToJson(userInfo)
					)
			)
			idx++
		}

		val response = jsonObject(
				"count" to reputations.size,
				"rank" to rankedUsers
		)

		call.respondJson(response)
	}
}