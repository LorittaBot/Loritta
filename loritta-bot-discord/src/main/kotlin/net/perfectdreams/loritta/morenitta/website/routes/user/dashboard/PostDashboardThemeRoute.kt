package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSettings
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.upsert
import java.time.Instant

class PostDashboardThemeRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard/theme") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val params = call.receiveParameters()
		val theme = ColorTheme.valueOf(params.getOrFail("theme"))

		loritta.transaction {
			UserWebsiteSettings.upsert(UserWebsiteSettings.id) {
				it[UserWebsiteSettings.id] = userIdentification.id.toLong()
				it[UserWebsiteSettings.dashboardColorThemePreference] = theme
				it[UserWebsiteSettings.dashboardColorThemePreferenceUpdatedAt] = Instant.now()
			}
		}

		call.response.header("HX-Refresh", "true")
		call.respondText("", status = HttpStatusCode.NoContent)
	}
}