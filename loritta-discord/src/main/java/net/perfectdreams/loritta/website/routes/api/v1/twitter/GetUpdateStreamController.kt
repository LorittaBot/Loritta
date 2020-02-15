package net.perfectdreams.loritta.website.routes.api.v1.twitter

import com.github.salomonbrys.kotson.jsonObject
import io.ktor.application.ApplicationCall
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class GetUpdateStreamController(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/twitter/update-stream") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		logger.info { "Updating Twitter Stream in the next 15 minutes..." }
		com.mrpowergamerbr.loritta.utils.loritta.tweetTracker.restartStream = true

		call.respondJson(jsonObject())
	}
}