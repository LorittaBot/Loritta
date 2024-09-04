package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch

import io.ktor.server.application.*
import kotlinx.html.body
import kotlinx.html.script
import kotlinx.html.stream.createHTML
import kotlinx.html.unsafe
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.AuthorizedTwitchAccounts
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
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

		call.respondHtml(
			createHTML()
				.body {
					script {
						unsafe {
							raw("""
								var openerOrigin = window.opener.origin;
								window.opener.postMessage("${response.id}", openerOrigin);
								window.close();
							""".trimIndent())
						}
					}
				}
		)
	}
}