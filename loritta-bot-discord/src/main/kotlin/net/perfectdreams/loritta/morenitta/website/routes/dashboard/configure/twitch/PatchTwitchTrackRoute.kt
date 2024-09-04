package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.util.*
import kotlinx.serialization.Serializable
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
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.SpicyMorenittaTriggers
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.twitch.GuildConfigureTwitchChannelView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.time.Instant
import kotlin.math.ceil

class PatchTwitchTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/twitch/tracks/{trackId}") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		// This is the route that adds a NEW instance to the configuration
		val postParams = call.receiveParameters()
		val trackId = call.parameters.getOrFail("trackId").toLong()
		val twitchUserId = postParams.getOrFail("twitchUserId").toLong()
		val createPremiumTrack = postParams.getOrFail("createPremiumTrack").toBoolean()
		val channelId = postParams.getOrFail("channelId").toLong()
		val message = postParams.getOrFail("message")

		val result = loritta.transaction {
			// First we need to try creating the premium track, if needed
			// Does not exist, so let's insert it!
			TrackedTwitchAccounts.update({
				TrackedTwitchAccounts.id eq trackId
			}) {
				it[TrackedTwitchAccounts.guildId] = guild.idLong
				it[TrackedTwitchAccounts.channelId] = channelId
				it[TrackedTwitchAccounts.twitchUserId] = twitchUserId
				it[TrackedTwitchAccounts.message] = message
				it[TrackedTwitchAccounts.editedAt] = Instant.now()
			}

			val state = TwitchWebUtils.getTwitchAccountTrackState(twitchUserId)

			val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
				.toList()
				.sumOf { it.value }
				.let { ceil(it) }

			val premiumTracksCount = PremiumTrackTwitchAccounts.select {
				PremiumTrackTwitchAccounts.guildId eq guild.idLong
			}.count()

			return@transaction UpdateGuildTwitchChannelResult.Success(trackId, state, valueOfTheDonationKeysEnabledOnThisGuild, premiumTracksCount)
		}

		val twitchUser = TwitchWebUtils.getCachedUsersInfoById(loritta, twitchUserId)
			.first()

		call.response.headerHXTrigger(SpicyMorenittaTriggers(playSoundEffect = "config-saved"))

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
				result.trackId,
				createPremiumTrack,
				twitchUser,
				result.state,
				GuildConfigureTwitchChannelView.TwitchTrackSettings(
					channelId,
					message
				),
				ServerPremiumPlans.getPlanFromValue(result.valueOfTheDonationKeysEnabledOnThisGuild),
				result.premiumTracksCount
			).generateHtml()
		)
	}

	sealed class UpdateGuildTwitchChannelResult {
		@Serializable
		class Success(val trackId: Long, val state: TwitchAccountTrackState, val valueOfTheDonationKeysEnabledOnThisGuild: Double, val premiumTracksCount: Long) : UpdateGuildTwitchChannelResult()
	}
}