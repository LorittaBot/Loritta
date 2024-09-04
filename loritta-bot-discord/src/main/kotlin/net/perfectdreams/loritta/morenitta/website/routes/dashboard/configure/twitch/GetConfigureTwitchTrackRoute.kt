package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch

import io.ktor.server.application.*
import io.ktor.server.util.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.twitch.GuildConfigureTwitchChannelView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class GetConfigureTwitchTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/twitch/tracks/{trackId}") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val trackId = call.parameters.getOrFail("trackId").toLong()

		val (trackedTwitchAccount, state) = loritta.transaction {
			val tracked = TrackedTwitchAccounts.selectAll()
				.where {
					TrackedTwitchAccounts.id eq trackId and (TrackedTwitchAccounts.guildId eq guild.idLong)
				}
				.first()

			val state = TwitchWebUtils.getTwitchAccountTrackState(tracked[TrackedTwitchAccounts.twitchUserId])
			Pair(tracked, state)
		}

		val twitchUser = TwitchWebUtils.getCachedUsersInfoById(loritta, trackedTwitchAccount[TrackedTwitchAccounts.twitchUserId])
			.first()

		call.respondHtml(
			GuildConfigureTwitchChannelView(
				loritta.newWebsite!!,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				userIdentification,
				UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
				colorTheme,
				guild,
				"twitch",
				trackId,
				false,
				twitchUser,
				state
			).generateHtml()
		)
	}
}