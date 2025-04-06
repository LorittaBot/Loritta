package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.server.application.*
import kotlinx.html.body
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.stream.createHTML
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserPocketLorittaSettings
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.serializable.PocketLorittaSettings
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.selectAll

class PocketLorittaRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard/pocket-loritta") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val pocketLorittaSettings = loritta.transaction {
			UserPocketLorittaSettings.selectAll()
				.where {
					UserPocketLorittaSettings.id eq userIdentification.id.toLong()
				}
				.limit(1)
				.firstOrNull()
				.let {
					PocketLorittaSettings(
						it?.get(UserPocketLorittaSettings.lorittaCount) ?: 0,
						it?.get(UserPocketLorittaSettings.pantufaCount) ?: 0,
						it?.get(UserPocketLorittaSettings.gabrielaCount) ?: 0,
					)
				}
		}

		call.respondHtml(
			createHTML()
				.body {
					div {
						canvas(classes = "loritta-game-canvas") {
							attributes["data-component-mounter"] = "loritta-game-canvas"
							attributes["pocket-loritta-settings"] = Json.encodeToString(pocketLorittaSettings)
						}
					}
				}
		)
	}
}