package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import io.ktor.application.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import java.io.File
import java.util.*

class PostUpdateUserBackgroundRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/users/{userId}/background") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val userId = call.parameters["userId"]
		val json = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()) }

		val type = json["type"].string

		if (type == "custom") {
			logger.info { "Updating $userId background with custom data..." }
			val data = json["data"].string

			File(com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.website.folder, "static/assets/img/backgrounds/${userId}.png")
					.writeBytes(Base64.getDecoder().decode(data))
		}

		call.respondJson(jsonObject())
	}
}