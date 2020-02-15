package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.utils.jsonParser
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveText
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import kotlin.collections.set

class PostUsernameChangeRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/loritta/users/{userId}/username-change") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		val userId = call.parameters["userId"] ?: return
		val json = jsonParser.parse(call.receiveText()).obj

		val name = json["name"].nullString
		val discriminator = json["discriminator"].nullString

		logger.info { "Received user info change for $userId! name = $name; discriminator = $discriminator"}

		val userIdAsLong = userId.toLong()

		if (!EventLogListener.handledUsernameChanges.containsKey(userIdAsLong)) {
			EventLogListener.handledUsernameChanges[userIdAsLong] = EventLogListener.UserMetaHolder(name, discriminator)
		} else {
			val usernameChange = EventLogListener.handledUsernameChanges[userIdAsLong]!!
			usernameChange.oldName = name
			usernameChange.oldDiscriminator = discriminator
		}

		call.respondJson(jsonObject())
	}
}