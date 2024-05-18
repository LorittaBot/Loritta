package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.EventLogConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.ConfigureEventLogRoute.FakeEventLogConfig
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildEventLogView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.time.Instant

class PostConfigureEventLogRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/event-log") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val params = call.receiveParameters()

		val eventLogEnabled = params["eventLogEnabled"] == "on"
		val eventLogChannelId = params["eventLogChannelId"]?.toLong()
		val memberBanned = params["memberBanned"] == "on"
		val memberBannedLogChannelId = params["memberBannedLogChannelId"]?.ifBlank { null }?.toLong()
		val memberUnbanned = params["memberUnbanned"] == "on"
		val memberUnbannedLogChannelId = params["memberUnbannedLogChannelId"]?.ifBlank { null }?.toLong()
		val messageEdited = params["messageEdited"] == "on"
		val messageEditedLogChannelId = params["messageEditedLogChannelId"]?.ifBlank { null }?.toLong()
		val messageDeleted = params["messageDeleted"] == "on"
		val messageDeletedLogChannelId = params["messageDeletedLogChannelId"]?.ifBlank { null }?.toLong()
		val nicknameChanges = params["nicknameChanges"] == "on"
		val nicknameChangesLogChannelId = params["nicknameChangesLogChannelId"]?.ifBlank { null }?.toLong()
		val avatarChanges = params["avatarChanges"] == "on"
		val avatarChangesLogChannelId = params["avatarChangesLogChannelId"]?.ifBlank { null }?.toLong()
		val voiceChannelJoins = params["voiceChannelJoins"] == "on"
		val voiceChannelJoinsLogChannelId = params["voiceChannelJoinsLogChannelId"]?.ifBlank { null }?.toLong()
		val voiceChannelLeaves = params["voiceChannelLeaves"] == "on"
		val voiceChannelLeavesLogChannelId = params["voiceChannelLeavesLogChannelId"]?.ifBlank { null }?.toLong()

		loritta.newSuspendedTransaction {
			val eventLogConfig = serverConfig.eventLogConfig

			if (!eventLogEnabled || eventLogChannelId == null) {
				serverConfig.eventLogConfig = null
				eventLogConfig?.delete()
			} else {
				val newConfig = eventLogConfig ?: EventLogConfig.new {
					this.eventLogChannelId = -1
				}

				newConfig.enabled = eventLogEnabled
				newConfig.eventLogChannelId = eventLogChannelId
				newConfig.memberBanned = memberBanned
				newConfig.memberBannedLogChannelId = memberBannedLogChannelId
				newConfig.memberUnbanned = memberUnbanned
				newConfig.memberUnbannedLogChannelId = memberUnbannedLogChannelId
				newConfig.messageEdited = messageEdited
				newConfig.messageEditedLogChannelId = messageEditedLogChannelId
				newConfig.messageDeleted = messageDeleted
				newConfig.messageDeletedLogChannelId = messageDeletedLogChannelId
				newConfig.nicknameChanges = nicknameChanges
				newConfig.nicknameChangesLogChannelId = nicknameChangesLogChannelId
				newConfig.avatarChanges = avatarChanges
				newConfig.avatarChangesLogChannelId = avatarChangesLogChannelId
				newConfig.voiceChannelJoins = voiceChannelJoins
				newConfig.voiceChannelJoinsLogChannelId = voiceChannelJoinsLogChannelId
				newConfig.voiceChannelLeaves = voiceChannelLeaves
				newConfig.voiceChannelLeavesLogChannelId = voiceChannelLeavesLogChannelId
				newConfig.updatedAt = Instant.now()

				serverConfig.eventLogConfig = newConfig
			}
		}

		call.response.header(
			"HX-Trigger",
			buildJsonObject {
				put("playSoundEffect", "config-saved")
				put(
					"showSpicyToast",
					EmbeddedSpicyModalUtils.encodeURIComponent(
						Json.encodeToString(
							EmbeddedSpicyToast(EmbeddedSpicyToast.Type.SUCCESS, "Configuração salva!", null)
						)
					)
				)
			}.toString()
		)

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
}