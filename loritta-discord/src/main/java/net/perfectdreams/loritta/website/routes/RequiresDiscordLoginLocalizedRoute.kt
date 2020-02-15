package net.perfectdreams.loritta.website.routes

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import io.ktor.application.ApplicationCall
import io.ktor.request.path
import io.ktor.response.respondRedirect
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.lorittaSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.util.*

abstract class RequiresDiscordLoginLocalizedRoute(loritta: LorittaDiscord, path: String) : LocalizedRoute(loritta, path) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	abstract suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification)

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		var start = System.currentTimeMillis()
		val session = call.lorittaSession
		logger.info { "Time to get session: ${System.currentTimeMillis() - start}" }
		start = System.currentTimeMillis()

		val discordAuth = session.getDiscordAuthFromJson()
		logger.info { "Time to get Discord Auth: ${System.currentTimeMillis() - start}" }
		start = System.currentTimeMillis()
		val userIdentification = session.getUserIdentification(call)
		logger.info { "Time to get User Identification: ${System.currentTimeMillis() - start}" }

		if (discordAuth == null || userIdentification == null) {
			// redirect to authentication owo
			val state = JsonObject()
			state["redirectUrl"] = LorittaWebsite.WEBSITE_URL.substring(0, LorittaWebsite.Companion.WEBSITE_URL.length - 1) + call.request.path()
			call.respondRedirect(com.mrpowergamerbr.loritta.utils.loritta.discordInstanceConfig.discord.authorizationUrl + "&state=${Base64.getEncoder().encodeToString(state.toString().toByteArray()).encodeToUrl()}", false)
			return
		}

		onAuthenticatedRequest(call, locale, discordAuth, userIdentification)
	}
}