package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.welcomer

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
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
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.WelcomerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildWelcomerView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.serializable.config.GuildWelcomerConfig
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class PatchWelcomerRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/welcomer") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val postParams = call.receiveParameters()

		val tellOnJoin = postParams["tellOnJoin"] == "on"
		val channelJoinId = postParams.getOrFail("channelJoinId").toLong()
		val joinMessage = postParams.getOrFail("joinMessage")
		val deleteJoinMessagesAfter = postParams.getOrFail("deleteJoinMessagesAfter").toLong()

		val tellOnRemove = postParams["tellOnRemove"] == "on"
		val channelRemoveId = postParams.getOrFail("channelRemoveId").toLong()
		val removeMessage = postParams.getOrFail("removeMessage")
		val deleteRemoveMessagesAfter = postParams.getOrFail("deleteRemoveMessagesAfter").toLong()

		val tellOnBan = postParams["tellOnBan"] == "on"
		val bannedMessage = postParams.getOrFail("bannedMessage")

		val tellOnPrivateJoin = postParams["tellOnPrivateJoin"] == "on"
		val joinPrivateMessage = postParams.getOrFail("joinPrivateMessage")

		val welcomerConfig = loritta.newSuspendedTransaction {
			val welcomerConfig = serverConfig.welcomerConfig

			val newConfig = welcomerConfig ?: WelcomerConfig.new {
				this.tellOnJoin = tellOnJoin
				this.channelJoinId = channelJoinId
				this.joinMessage = joinMessage
				this.deleteJoinMessagesAfter = deleteJoinMessagesAfter
				this.tellOnRemove = tellOnRemove
				this.channelRemoveId = channelRemoveId
				this.removeMessage = removeMessage
				this.deleteRemoveMessagesAfter = deleteRemoveMessagesAfter
				this.tellOnBan = tellOnBan
				this.bannedMessage = bannedMessage
				this.tellOnPrivateJoin = tellOnPrivateJoin
				this.joinPrivateMessage = joinPrivateMessage
			}

			newConfig.tellOnJoin = tellOnJoin
			newConfig.channelJoinId = channelJoinId
			newConfig.joinMessage = joinMessage
			newConfig.deleteJoinMessagesAfter = deleteJoinMessagesAfter
			newConfig.tellOnRemove = tellOnRemove
			newConfig.channelRemoveId = channelRemoveId
			newConfig.removeMessage = removeMessage
			newConfig.deleteRemoveMessagesAfter = deleteRemoveMessagesAfter
			newConfig.tellOnBan = tellOnBan
			newConfig.bannedMessage = bannedMessage
			newConfig.tellOnPrivateJoin = tellOnPrivateJoin
			newConfig.joinPrivateMessage = joinPrivateMessage

			serverConfig.welcomerConfig = newConfig

			newConfig
		}

		val guildWelcomerConfig = welcomerConfig.let {
			GuildWelcomerConfig(
				it.tellOnJoin,
				it.channelJoinId,
				it.joinMessage,
				it.deleteJoinMessagesAfter,

				it.tellOnRemove,
				it.channelRemoveId,
				it.removeMessage,
				it.deleteRemoveMessagesAfter,

				it.tellOnPrivateJoin,
				it.joinPrivateMessage,

				it.tellOnBan,
				it.bannedMessage,
			)
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

		call.respondHtml(
			GuildWelcomerView(
				loritta.newWebsite!!,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				userIdentification,
				UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
				colorTheme,
				guild,
				"welcomer",
				guildWelcomerConfig
			).generateHtml()
		)
	}
}