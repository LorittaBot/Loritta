package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.networkbans.NetworkBanEntry
import io.ktor.application.ApplicationCall
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.BlacklistedUsers
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class GetGlobalBansRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/global-bans/sync/{userId}") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val userId = call.parameters["userId"] ?: return

		val user = lorittaShards.getUserById(userId) ?: run {
			call.respondJson(
					jsonObject()
			)
			return
		}

		val bannedUser = transaction(Databases.loritta) {
			BlacklistedUsers.select { BlacklistedUsers.id eq userId.toLong() }.firstOrNull()
		}

		if (bannedUser == null) {
			logger.warn { "Received request to sync bans for ${user.idLong}, but user isn't on the global ban list!" }
			return
		}

		logger.info("Received request to sync bans for ${user.idLong}")

		val entry = NetworkBanEntry(
				bannedUser[BlacklistedUsers.id].value,
				bannedUser[BlacklistedUsers.guildId],
				bannedUser[BlacklistedUsers.type],
				bannedUser[BlacklistedUsers.reason]
		)


		com.mrpowergamerbr.loritta.utils.loritta.networkBanManager.punishUser(user, com.mrpowergamerbr.loritta.utils.loritta.networkBanManager.createBanReason(entry, true), bannedUser[BlacklistedUsers.globally])

	}
}