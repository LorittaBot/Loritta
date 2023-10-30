package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleGuildDashboardRawHtmlView
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleRawHtmlView
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

open class GenericConfigurationRoute(loritta: LorittaBot, path: String, val type: String, val file: String) : RequiresGuildAuthLocalizedRoute(loritta, path) {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		val variables = call.legacyVariables(loritta, locale)
		variables["saveType"] = type

		if (type == "miscellaneous") {
			val miscellaneousConfig = loritta.newSuspendedTransaction {
				serverConfig.miscellaneousConfig
			}

			variables["serverConfig"] = ConfigureMiscellaneousRoute.FakeServerConfig(
					ConfigureMiscellaneousRoute.FakeServerConfig.FakeMiscellaneousConfig(
							miscellaneousConfig?.enableBomDiaECia ?: false,
							miscellaneousConfig?.enableQuirky ?: false
					)
			)
		}

		call.respondHtml(
			LegacyPebbleGuildDashboardRawHtmlView(
				loritta,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				guild,
				"Painel de Controle",
				evaluate(file, variables),
				type
			).generateHtml()
		)
	}
}