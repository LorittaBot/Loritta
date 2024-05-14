package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserPocketLorittaSettings
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.serializable.PocketLorittaSettings
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.upsert
import java.time.Instant

class PostClearPocketLorittaRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard/pocket-loritta/clear") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val userId = userIdentification.id.toLong()

		val settings = loritta.transaction {
			UserPocketLorittaSettings.upsert(UserPocketLorittaSettings.id) {
				it[UserPocketLorittaSettings.id] = userId
				it[UserPocketLorittaSettings.lorittaCount] = 0
				it[UserPocketLorittaSettings.pantufaCount] = 0
				it[UserPocketLorittaSettings.gabrielaCount] = 0
				it[UserPocketLorittaSettings.updatedAt] = Instant.now()
			}

			PocketLorittaSettings(0, 0, 0)
		}

		call.response.header(
			"HX-Trigger",
			buildJsonObject {
				put("playSoundEffect", "recycle-bin")
				put("pocketLorittaSettingsSync", Json.encodeToString(settings))
			}.toString()
		)

		call.respondText("", status = HttpStatusCode.NoContent)
	}
}