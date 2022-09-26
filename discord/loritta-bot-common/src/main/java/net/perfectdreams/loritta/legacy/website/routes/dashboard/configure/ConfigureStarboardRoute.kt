package net.perfectdreams.loritta.legacy.website.routes.dashboard.configure

import net.perfectdreams.loritta.legacy.Loritta
import net.perfectdreams.loritta.legacy.dao.ServerConfig
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.legacy.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.legacy.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import kotlin.collections.set

class ConfigureStarboardRoute(loritta: LorittaDiscord) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/starboard") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		loritta as Loritta

		val starboardConfig = loritta.newSuspendedTransaction {
			serverConfig.starboardConfig
		}

		val variables = call.legacyVariables(locale)

		variables["saveType"] = "starboard"
		variables["serverConfig"] = FakeServerConfig(
				FakeServerConfig.FakeStarboardConfig(
						starboardConfig?.enabled ?: false,
						starboardConfig?.starboardChannelId?.toString(),
						starboardConfig?.requiredStars ?: 1
				)
		)

		call.respondHtml(evaluate("starboard.html", variables))
	}

	/**
	 * Fake Server Config for Pebble, in the future this will be removed
	 */
	private class FakeServerConfig(val starboardConfig: FakeStarboardConfig) {
		class FakeStarboardConfig(
				val isEnabled: Boolean,
				val starboardId: String?,
				val requiredStars: Int
		)
	}
}