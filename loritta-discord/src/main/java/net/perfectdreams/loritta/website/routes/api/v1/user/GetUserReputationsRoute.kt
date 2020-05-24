package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.tables.Reputations
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.select

class GetUserReputationsRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/{userId}/reputation") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val receiver = call.parameters["userId"] ?: return

		val count = loritta.newSuspendedTransaction {
			Reputations.select { Reputations.receivedById eq receiver.toLong() }.count()
		}

		call.respondJson(jsonObject("count" to count))
	}
}