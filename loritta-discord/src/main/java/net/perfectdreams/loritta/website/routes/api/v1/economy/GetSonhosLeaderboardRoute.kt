package net.perfectdreams.loritta.website.routes.api.v1.economy

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.transactions.TransactionManager

class GetSonhosLeaderboardRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/economy/sonhos-leaderboard/{type}") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val payload = jsonObject()
		val type = call.parameters["type"]
		val selfUserId = userIdentification.id.toLong()
		val profile = loritta.getLorittaProfileAsync(userIdentification.id.toLong())

		if (profile != null && profile.money != 0L) {
			if (type == "around") {
				val padding = call.parameters["padding"]!!.toInt()
				if (padding !in 0..5)
					return

				var rankPosition: Long? = null
				val usersAround = mutableListOf<SonhosLeaderboardUser>()

				loritta.newSuspendedTransaction {
					// It would be nicer if we did that directly via Exposed, but whatever.
					// Because IDs are always a long, there is no risk of SQL Injection
					// First we are going to get the user's ranking position (if it exists)
					// The "LIMIT 1" seems useless, but it actually speeds up the query a ton if the user has a lot of sonhos.
					// With ID: 351760430991147010
					// Without limit: ~800ms
					// With limit: ~1ms
					TransactionManager.current().exec("select * FROM (select profiles.id, RANK() over (ORDER BY money desc) rank_number FROM profiles WHERE money > 0) a WHERE id = ${userIdentification.id} LIMIT 1;") { rs ->
						while (rs.next()) {
							rankPosition = rs.getLong("rank_number")
						}
					}

					if (padding != 0) {
						// If Rank Position != null, we are going to get the users that are "round" the user's rank position
						// We are going to select the ${padding} users above and the ${padding} users below.
						// Again, there is a limit because it improves performance!
						// The limit is ${padding + padding + 1} (The one is the "self" user ranking)
						val rankPosition = rankPosition
						if (rankPosition != null) {
							TransactionManager.current().exec("select * FROM (select profiles.id, profiles.money, RANK() over (ORDER BY money desc) rank_number FROM profiles WHERE money > 0) a WHERE rank_number BETWEEN ${rankPosition - padding} AND ${rankPosition + padding} LIMIT ${padding + padding + 1}") { rs ->
								while (rs.next()) {
									usersAround.add(
											SonhosLeaderboardUser(
													rs.getLong("id"),
													rs.getLong("money"),
													rs.getInt("rank_number")
											)
									)
								}
							}
						}
					}
				}

				if (rankPosition != null) {
					payload["rankPosition"] = rankPosition

					// O problema é quando tem muitas pessoas no mesmo ranking, por isso apenas iremos pegar quem está a frente do user e quem está atrás
					// Por exemplo: Se tem 10 pessoas no ranking #10, a Loritta pega essas 10 pessoas, em vez de respeitar o padding
					val fixedUsersAround = mutableListOf<SonhosLeaderboardUser>()
					val indexOfSelf = usersAround.indexOfFirst { it.id == selfUserId }
					if (indexOfSelf == -1) { // what? é meio impossivel isto acontecer, mas né...
						call.respondJson(payload)
						return
					}

					fixedUsersAround.addAll(usersAround.subList(Math.max(0, indexOfSelf - padding), indexOfSelf))
					fixedUsersAround.addAll(usersAround.subList(indexOfSelf, Math.min(usersAround.size, (indexOfSelf + padding + 1))))

					val usersAroundJson = jsonArray()
					for (user in fixedUsersAround) {
						val retrieved = lorittaShards.retrieveUserInfoById(user.id) ?: continue
						usersAroundJson.add(
								jsonObject(
										"id" to user.id,
										"money" to user.money,
										"position" to user.position,
										"name" to retrieved.name,
										"avatarUrl" to retrieved.effectiveAvatarUrl,
										"discriminator" to retrieved.discriminator
								)
						)
					}

					payload["usersAround"] = usersAroundJson
				}
			}
		}

		call.respondJson(payload)
	}

	private data class SonhosLeaderboardUser(
			val id: Long,
			val money: Long,
			val position: Int
	)
}