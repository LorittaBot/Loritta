package net.perfectdreams.loritta.legacy.website.routes.dashboard.configure

import net.perfectdreams.loritta.legacy.Loritta
import net.perfectdreams.loritta.legacy.dao.ServerConfig
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.perfectdreams.loritta.legacy.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.legacy.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.legacy.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class ConfigureAutoroleRoute(loritta: LorittaDiscord) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/autorole") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		loritta as Loritta

		val autoroleConfig = loritta.newSuspendedTransaction {
			serverConfig.autoroleConfig
		}

		val variables = call.legacyVariables(locale)

		variables["saveType"] = "autorole"
		variables["serverConfig"] = FakeServerConfig(
				FakeServerConfig.FakeAutoroleConfig(
						autoroleConfig?.enabled ?: false,
						autoroleConfig?.giveOnlyAfterMessageWasSent ?: false,
						autoroleConfig?.giveRolesAfter ?: 0
				)
		)

		val validEnabledRoles = autoroleConfig?.roles?.filter {
			try {
				guild.getRoleById(it) != null
			} catch (e: Exception) {
				false
			}
		} ?: listOf()
		variables["currentAutoroles"] = validEnabledRoles.joinToString(separator = ";")

		call.respondHtml(evaluate("autorole.html", variables))
	}

	/**
	 * Fake Server Config for Pebble, in the future this will be removed
	 */
	private class FakeServerConfig(val autoroleConfig: FakeAutoroleConfig) {
		class FakeAutoroleConfig(
				val isEnabled: Boolean,
				val giveOnlyAfterMessageWasSent: Boolean,
				val giveRolesAfter: Long
		)
	}
}