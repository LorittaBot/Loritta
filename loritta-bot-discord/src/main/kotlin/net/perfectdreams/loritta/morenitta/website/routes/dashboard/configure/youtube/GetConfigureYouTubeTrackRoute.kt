package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube

import io.ktor.server.application.*
import io.ktor.server.util.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.youtube.GuildConfigureYouTubeChannelView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class GetConfigureYouTubeTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/youtube/tracks/{trackId}") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val trackId = call.parameters.getOrFail("trackId").toLong()

		val trackedYouTubeAccount = loritta.transaction {
			TrackedYouTubeAccounts.selectAll()
				.where {
					TrackedYouTubeAccounts.guildId eq guild.idLong and (TrackedYouTubeAccounts.id eq trackId)
				}
				.first()
		}

		val result = YouTubeWebUtils.getYouTubeChannelInfoFromChannelId(loritta, trackedYouTubeAccount[TrackedYouTubeAccounts.youTubeChannelId]) as YouTubeWebUtils.YouTubeChannelInfoResult.Success

		call.respondHtml(
			GuildConfigureYouTubeChannelView(
				loritta.newWebsite!!,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				userIdentification,
				UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
				colorTheme,
				guild,
				trackId,
				YouTubeChannel(
					result.channel.channelId,
					result.channel.name,
					result.channel.avatarUrl
				),
				GuildConfigureYouTubeChannelView.YouTubeTrackSettings(
					trackedYouTubeAccount[TrackedYouTubeAccounts.channelId],
					trackedYouTubeAccount[TrackedYouTubeAccounts.message],
				)
			).generateHtml()
		)
	}
}