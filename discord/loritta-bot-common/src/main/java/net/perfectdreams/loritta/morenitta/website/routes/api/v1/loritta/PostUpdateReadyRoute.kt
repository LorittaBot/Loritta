package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import net.perfectdreams.loritta.morenitta.utils.PatchData
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class PostUpdateReadyRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/update") {
	companion object {
		val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val json = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()) }

		val type = json["type"].string

		when (type) {
			"restart" -> {
				logger.info { "Received request to restart, waiting 2.5s and then shutting down..." }

				thread {
					Thread.sleep(2_500)
					exitProcess(0)
				}

				call.respondJson(jsonObject())
			}
			"setRestartTimer" -> {
				val willRestartAt = json["willRestartAt"].long

				net.perfectdreams.loritta.morenitta.utils.loritta.patchData.willRestartAt = willRestartAt
				call.respondJson(jsonObject())
			}
			"setPatchNotesPost" -> {
				val patchNotesPostId = json["patchNotesPostId"].string
				val expiresAt = json["expiresAt"].long

				net.perfectdreams.loritta.morenitta.utils.loritta.patchData.patchNotes = PatchData.PatchNotes(
						System.currentTimeMillis(),
						expiresAt,
						patchNotesPostId
				)

				call.respondJson(jsonObject())
			}
		}
	}
}