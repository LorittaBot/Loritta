package com.mrpowergamerbr.loritta.website.requests.routes.page.api.v1.loritta

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.networkbans.NetworkBanEntry
import com.mrpowergamerbr.loritta.website.LoriAuthLevel
import com.mrpowergamerbr.loritta.website.LoriDoNotLocaleRedirect
import com.mrpowergamerbr.loritta.website.LoriRequiresAuth
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.BlacklistedUsers
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.MediaType
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Path

@Path("/api/v1/loritta/global-bans/sync/:userId")
class GlobalBansController {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	@GET
	@LoriDoNotLocaleRedirect(true)
	@LoriRequiresAuth(LoriAuthLevel.API_KEY)
	fun handle(req: Request, res: Response, userId: String) {
		res.type(MediaType.json)

		val user = lorittaShards.getUserById(userId) ?: run {
			res.send(
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


		loritta.networkBanManager.punishUser(user, loritta.networkBanManager.createBanReason(entry, true), bannedUser[BlacklistedUsers.globally])
	}
}