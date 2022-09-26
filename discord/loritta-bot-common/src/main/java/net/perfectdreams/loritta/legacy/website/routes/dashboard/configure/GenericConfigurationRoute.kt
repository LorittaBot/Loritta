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

open class GenericConfigurationRoute(loritta: LorittaDiscord, path: String, val type: String, val file: String) : RequiresGuildAuthLocalizedRoute(loritta, path) {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		loritta as Loritta
		val variables = call.legacyVariables(locale)
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

		call.respondHtml(evaluate(file, variables))
	}
}