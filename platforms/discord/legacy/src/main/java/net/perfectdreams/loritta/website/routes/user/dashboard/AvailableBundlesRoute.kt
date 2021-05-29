package net.perfectdreams.loritta.website.routes.user.dashboard

import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class AvailableBundlesRoute(loritta: LorittaDiscord) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/@me/dashboard/bundles") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val variables = call.legacyVariables(locale)
		variables["saveType"] = "bundles"

		call.respondHtml(evaluate("profile_dashboard_bundles.html", variables))
	}
}