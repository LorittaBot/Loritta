package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleRawHtmlView
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class ConfigureAutoroleRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/autorole") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		loritta as LorittaBot

		val autoroleConfig = loritta.newSuspendedTransaction {
			serverConfig.autoroleConfig
		}

		val variables = call.legacyVariables(loritta, locale)

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

		call.respondHtml(
			LegacyPebbleRawHtmlView(
				loritta,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				"Painel de Controle",
				evaluate("autorole.html", variables)
			).generateHtml()
		)
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