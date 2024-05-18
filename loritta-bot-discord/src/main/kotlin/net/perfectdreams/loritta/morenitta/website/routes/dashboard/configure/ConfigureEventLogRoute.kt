package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import io.ktor.server.application.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildEventLogView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class ConfigureEventLogRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/event-log") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val eventLogConfig = loritta.newSuspendedTransaction {
			serverConfig.eventLogConfig
		}

		call.respondHtml(
			GuildEventLogView(
				loritta.newWebsite!!,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				userIdentification,
				UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
				colorTheme,
				guild,
				"event_log",
				FakeEventLogConfig(
					eventLogConfig?.enabled ?: false,
					eventLogConfig?.eventLogChannelId,
					eventLogConfig?.memberBanned ?: false,
					eventLogConfig?.memberUnbanned ?: false,
					eventLogConfig?.messageEdited ?: false,
					eventLogConfig?.messageDeleted ?: false,
					eventLogConfig?.nicknameChanges ?: false,
					eventLogConfig?.avatarChanges ?: false,
					eventLogConfig?.voiceChannelJoins ?: false,
					eventLogConfig?.voiceChannelLeaves ?: false,

					eventLogConfig?.memberBannedLogChannelId,
					eventLogConfig?.memberUnbannedLogChannelId,
					eventLogConfig?.messageEditedLogChannelId,
					eventLogConfig?.messageDeletedLogChannelId,
					eventLogConfig?.nicknameChangesLogChannelId,
					eventLogConfig?.avatarChangesLogChannelId,
					eventLogConfig?.voiceChannelJoinsLogChannelId,
					eventLogConfig?.voiceChannelLeavesLogChannelId,
				)
			).generateHtml()
		)
	}

	class FakeEventLogConfig(
		val isEnabled: Boolean,
		val eventLogChannelId: Long?,
		val memberBanned: Boolean,
		val memberUnbanned: Boolean,
		val messageEdited: Boolean,
		val messageDeleted: Boolean,
		val nicknameChanges: Boolean,
		val avatarChanges: Boolean,
		val voiceChannelJoins: Boolean,
		val voiceChannelLeaves: Boolean,

		val memberBannedLogChannelId: Long?,
		val memberUnbannedLogChannelId: Long?,
		val messageEditedLogChannelId: Long?,
		val messageDeletedLogChannelId: Long?,
		val nicknameChangesLogChannelId: Long?,
		val avatarChangesLogChannelId: Long?,
		val voiceChannelJoinsLogChannelId: Long?,
		val voiceChannelLeavesLogChannelId: Long?
	)
}