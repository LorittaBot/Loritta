package net.perfectdreams.loritta.website.routes.dashboard

import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveParameters
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.transactions.transaction

class PostDashboardRoute(loritta: LorittaDiscord) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val receiveParameters = call.receiveParameters()
		val hideSharedServers = receiveParameters["hideSharedServers"]
		val hidePreviousUsernames = receiveParameters["hidePreviousUsernames"]
		val lorittaProfile = com.mrpowergamerbr.loritta.utils.loritta.getOrCreateLorittaProfile(userIdentification.id.toLong())

		if (hideSharedServers != null && hidePreviousUsernames != null) {
			val settings = transaction(Databases.loritta) { lorittaProfile.settings }

			transaction(Databases.loritta) {
				settings.hideSharedServers = hideSharedServers.toBoolean()
				settings.hidePreviousUsernames = hidePreviousUsernames.toBoolean()
			}

			val response = JsonObject()
			response["api:message"] = "OK"
			response["hideSharedServers"] = hideSharedServers
			response["hidePreviousUsernames"] = hidePreviousUsernames
			call.respondJson(response)
		} else {}
	}
}