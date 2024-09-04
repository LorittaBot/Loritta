package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube

import io.ktor.server.application.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.youtube.GuildYouTubeView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import kotlin.math.ceil

class ConfigureYouTubeRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/youtube") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val (trackedYouTubeAccounts, valueOfTheDonationKeysEnabledOnThisGuild) = loritta.transaction {
			val trackedYouTubeAccounts = TrackedYouTubeAccounts.selectAll()
				.where {
					TrackedYouTubeAccounts.guildId eq guild.idLong
				}
				.toList()

			val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
				.toList()
				.sumOf { it.value }
				.let { ceil(it) }

			Pair(trackedYouTubeAccounts, valueOfTheDonationKeysEnabledOnThisGuild)
		}

		val youtubeChannels = trackedYouTubeAccounts.map {
			GlobalScope.async {
				YouTubeWebUtils.getYouTubeChannelInfoFromChannelId(loritta, it[TrackedYouTubeAccounts.youTubeChannelId])
			}
		}.awaitAll().mapNotNull { (it as? YouTubeWebUtils.YouTubeChannelInfoResult.Success)?.channel }

		call.respondHtml(
			GuildYouTubeView(
				loritta.newWebsite!!,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				userIdentification,
				UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
				colorTheme,
				guild,
				ServerPremiumPlans.getPlanFromValue(valueOfTheDonationKeysEnabledOnThisGuild),
				trackedYouTubeAccounts,
				youtubeChannels
			).generateHtml()
		)
	}
}