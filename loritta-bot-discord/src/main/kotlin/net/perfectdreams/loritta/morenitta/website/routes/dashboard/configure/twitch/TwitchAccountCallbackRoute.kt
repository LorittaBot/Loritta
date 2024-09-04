package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.AuthorizedTwitchAccounts
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.switchtwitch.SwitchTwitchAPI
import org.jetbrains.exposed.sql.upsert
import java.time.Instant

class TwitchAccountCallbackRoute(val loritta: LorittaBot) : BaseRoute("/twitch-callback") {
	override suspend fun onRequest(call: ApplicationCall) {
		val api = SwitchTwitchAPI.fromAuthCode(
			loritta.config.loritta.twitch.clientId,
			loritta.config.loritta.twitch.clientSecret,
			call.request.queryParameters["code"]!!,
			loritta.config.loritta.twitch.redirectUri
		)

		val response = api.getSelfUserInfo()

		loritta.pudding.transaction {
			AuthorizedTwitchAccounts.upsert(AuthorizedTwitchAccounts.userId) {
				it[AuthorizedTwitchAccounts.userId] = response.id
				it[AuthorizedTwitchAccounts.authorizedAt] = Instant.now()
			}
		}

		call.respondText("<script>window.opener.postMessage(\"${response.id}\"); window.close();</script>", ContentType.Text.Html)
	}
}