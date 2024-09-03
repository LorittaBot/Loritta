package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.welcomer

import io.ktor.server.application.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.WelcomerConfigs
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildWelcomerView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.GuildWelcomerConfig
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.select

class ConfigureWelcomerRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/welcomer") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val result = loritta.transaction {
			ServerConfigs.innerJoin(WelcomerConfigs).select {
				ServerConfigs.id eq guild.idLong
			}.firstOrNull()
		}

		val guildWelcomerConfig = result?.let {
			GuildWelcomerConfig(
				it[WelcomerConfigs.tellOnJoin],
				it[WelcomerConfigs.channelJoinId],
				it[WelcomerConfigs.joinMessage],
				it[WelcomerConfigs.deleteJoinMessagesAfter],

				it[WelcomerConfigs.tellOnRemove],
				it[WelcomerConfigs.channelRemoveId],
				it[WelcomerConfigs.removeMessage],
				it[WelcomerConfigs.deleteRemoveMessagesAfter],

				it[WelcomerConfigs.tellOnPrivateJoin],
				it[WelcomerConfigs.joinPrivateMessage],

				it[WelcomerConfigs.tellOnBan],
				it[WelcomerConfigs.bannedMessage],
			)
		} ?: GuildWelcomerConfig(
			false,
			null,
			GuildWelcomerView.defaultJoinTemplate.content,
			null,

			false,
			null,
			GuildWelcomerView.defaultRemoveTemplate.content,
			null,

			false,
			null,

			false,
			null
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