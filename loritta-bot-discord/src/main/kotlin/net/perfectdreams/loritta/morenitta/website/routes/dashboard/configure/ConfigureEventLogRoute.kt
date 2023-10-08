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
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleGuildDashboardRawHtmlView
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleRawHtmlView
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class ConfigureEventLogRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedRoute(loritta, "/configure/event-log") {
	override suspend fun onGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig) {
		val eventLogConfig = loritta.newSuspendedTransaction {
			serverConfig.eventLogConfig
		}

		val variables = call.legacyVariables(loritta, locale)

		variables["saveType"] = "event_log"
		variables["serverConfig"] = FakeServerConfig(
				FakeServerConfig.FakeEventLogConfig(
						eventLogConfig?.enabled ?: false,
						eventLogConfig?.eventLogChannelId?.toString(),
						eventLogConfig?.memberBanned ?: false,
						eventLogConfig?.memberUnbanned ?: false,
						eventLogConfig?.messageEdited ?: false,
						eventLogConfig?.messageDeleted ?: false,
						eventLogConfig?.nicknameChanges ?: false,
						eventLogConfig?.avatarChanges ?: false,
						eventLogConfig?.voiceChannelJoins ?: false,
						eventLogConfig?.voiceChannelLeaves ?: false
				)
		)

		call.respondHtml(
			LegacyPebbleGuildDashboardRawHtmlView(
				loritta,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				guild,
				"Painel de Controle",
				evaluate("event_log.html", variables),
				"event_log"
			).generateHtml()
		)
	}

	/**
	 * Fake Server Config for Pebble, in the future this will be removed
	 */
	private class FakeServerConfig(val eventLogConfig: FakeEventLogConfig) {
		class FakeEventLogConfig(
				val isEnabled: Boolean,
				val eventLogChannelId: String?,
				val memberBanned: Boolean,
				val memberUnbanned: Boolean,
				val messageEdit: Boolean,
				val messageDeleted: Boolean,
				val nicknameChanges: Boolean,
				val avatarChanges: Boolean,
				val voiceChannelJoins: Boolean,
				val voiceChannelLeaves: Boolean
		)
	}
}