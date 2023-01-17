package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.website.evaluate
import io.ktor.server.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleRawHtmlView
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

open class GenericConfigurationRoute(loritta: LorittaBot, path: String, val type: String, val file: String) : RequiresGuildAuthLocalizedRoute(loritta, path) {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		loritta as LorittaBot
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
			LegacyPebbleRawHtmlView(
				loritta,
				locale,
				getPathWithoutLocale(call),
				"Painel de Controle",
				evaluate(file, variables)
			).generateHtml()
		)
	}
}