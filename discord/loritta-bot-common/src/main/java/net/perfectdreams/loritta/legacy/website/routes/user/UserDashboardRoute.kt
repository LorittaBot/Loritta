package net.perfectdreams.loritta.legacy.website.routes.user

import net.perfectdreams.loritta.legacy.website.utils.WebsiteUtils
import net.perfectdreams.loritta.legacy.utils.gson
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.utils.lorittaShards
import net.perfectdreams.loritta.legacy.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.legacy.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.legacy.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class UserDashboardRoute(loritta: LorittaDiscord) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/@me/dashboard") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val userId = userIdentification.id
		val variables = call.legacyVariables(locale)

		val user =  lorittaShards.retrieveUserById(userId)!!
		val lorittaProfile = net.perfectdreams.loritta.legacy.utils.loritta.getOrCreateLorittaProfile(userId)

		variables["profileUser"] = user
		variables["lorittaProfile"] = lorittaProfile
		variables["profileSettings"] = loritta.newSuspendedTransaction {
			lorittaProfile.settings
		}
		variables["profile_json"] = gson.toJson(
				WebsiteUtils.getProfileAsJson(lorittaProfile)
		)
		variables["saveType"] = "main"

		call.respondHtml(evaluate("profile_dashboard.html", variables))
	}
}