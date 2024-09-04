package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch

import io.ktor.server.application.*
import io.ktor.server.util.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.PremiumTrackTwitchAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.twitch.GuildConfigureTwitchChannelView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import kotlin.math.ceil

class GetConfigureTwitchTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/twitch/tracks/{trackId}") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		data class ConfigureTwitchTrackResult(
			val tracked: ResultRow,
			val state: TwitchAccountTrackState,
			val valueOfTheDonationKeysEnabledOnThisGuild: Double,
			val premiumTracksCount: Long
		)

		val trackId = call.parameters.getOrFail("trackId").toLong()

		val result = loritta.transaction {
			val tracked = TrackedTwitchAccounts.selectAll()
				.where {
					TrackedTwitchAccounts.id eq trackId and (TrackedTwitchAccounts.guildId eq guild.idLong)
				}
				.first()

			val state = TwitchWebUtils.getTwitchAccountTrackState(tracked[TrackedTwitchAccounts.twitchUserId])

			val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
				.toList()
				.sumOf { it.value }
				.let { ceil(it) }

			val premiumTracksCount = PremiumTrackTwitchAccounts.select {
				PremiumTrackTwitchAccounts.guildId eq guild.idLong
			}.count()

			ConfigureTwitchTrackResult(
				tracked,
				state,
				valueOfTheDonationKeysEnabledOnThisGuild,
				premiumTracksCount
			)
		}

		val twitchUser = TwitchWebUtils.getCachedUsersInfoById(loritta, result.tracked[TrackedTwitchAccounts.twitchUserId])
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
				result.state,
				GuildConfigureTwitchChannelView.TwitchTrackSettings(
					result.tracked[TrackedTwitchAccounts.channelId],
					result.tracked[TrackedTwitchAccounts.message],
				),
				ServerPremiumPlans.getPlanFromValue(result.valueOfTheDonationKeysEnabledOnThisGuild),
				result.premiumTracksCount
			).generateHtml()
		)
	}
}