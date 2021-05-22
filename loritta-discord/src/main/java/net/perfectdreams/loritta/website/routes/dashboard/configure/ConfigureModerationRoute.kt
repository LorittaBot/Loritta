package net.perfectdreams.loritta.website.routes.dashboard.configure

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.ServerConfig
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.website.evaluate
import io.ktor.application.ApplicationCall
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.dashboard.RequiresGuildAuthLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import kotlin.collections.set

class ConfigureModerationRoute(loritta: LorittaDiscord) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/moderation") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		loritta as Loritta

		val moderationConfig = loritta.newSuspendedTransaction {
			serverConfig.moderationConfig
		}

		val variables = call.legacyVariables(locale)

		variables["saveType"] = "moderation"
		variables["serverConfig"] = FakeServerConfig(
				FakeServerConfig.FakeModerationConfig(
						moderationConfig?.sendPunishmentViaDm ?: false,
						moderationConfig?.sendPunishmentToPunishLog ?: false,
						moderationConfig?.punishLogMessage ?: ""
				)
		)

		call.respondHtml(evaluate("configure_moderation.html", variables))
	}

	/**
	 * Fake Server Config for Pebble, in the future this will be removed
	 */
	private class FakeServerConfig(val moderationConfig: FakeModerationConfig) {
		class FakeModerationConfig(
				val sendPunishmentViaDm: Boolean,
				val sendToPunishLog: Boolean,
				val punishmentLogMessage: String
		)
	}
}