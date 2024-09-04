package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.PremiumTrackTwitchAccounts
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.twitch.GuildConfigureTwitchChannelView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import kotlin.math.ceil

class GetAddTwitchTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/twitch/add") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		data class AddNewGuildTwitchChannelTransactionResult(
			val valueOfTheDonationKeysEnabledOnThisGuild: Double,
			val premiumTracksCount: Long,
			val state: TwitchAccountTrackState
		)

		val userId = call.parameters.getOrFail("twitchUserId").toLong()
		val createPremiumTrack = call.parameters.getOrFail("createPremiumTrack").toBoolean()

		val transactionResult = loritta.transaction {
			val state = TwitchWebUtils.getTwitchAccountTrackState(userId)

			val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
				.toList()
				.sumOf { it.value }
				.let { ceil(it) }

			val premiumTracksCount = PremiumTrackTwitchAccounts.select {
				PremiumTrackTwitchAccounts.guildId eq guild.idLong
			}.count()

			AddNewGuildTwitchChannelTransactionResult(
				valueOfTheDonationKeysEnabledOnThisGuild,
				premiumTracksCount,
				state
			)
		}

		val twitchUser = TwitchWebUtils.getCachedUsersInfoById(loritta, userId)
			.first()

		if (call.request.header("HX-Request")?.toBoolean() == true) {
			call.response.headerHXTrigger {
				closeSpicyModal = true
				playSoundEffect = "config-saved"
			}
		}

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
				null,
				createPremiumTrack,
				twitchUser,
				if (createPremiumTrack) TwitchAccountTrackState.PREMIUM_TRACK_USER else transactionResult.state,
				GuildConfigureTwitchChannelView.TwitchTrackSettings(
					null,
					"Estou ao vivo jogando {stream.game}! **{stream.title}** {stream.url}"
				),
				ServerPremiumPlans.getPlanFromValue(transactionResult.valueOfTheDonationKeysEnabledOnThisGuild),
				transactionResult.premiumTracksCount
			).generateHtml()
		)
	}
}