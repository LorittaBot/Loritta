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
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.twitch.GuildConfigureTwitchChannelView
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.*
import java.time.Instant
import kotlin.math.ceil

class PutTwitchTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/twitch/tracks") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		// This is the route that adds a NEW instance to the configuration
		val postParams = call.receiveParameters()
		val twitchUserId = postParams.getOrFail("twitchUserId").toLong()
		val createPremiumTrack = postParams.getOrFail("createPremiumTrack").toBoolean()
		val channelId = postParams.getOrFail("channelId").toLong()
		val message = postParams.getOrFail("message")

		val result = loritta.transaction {
			// First we need to try creating the premium track, if needed
			if (createPremiumTrack) {
				val isAlreadyAdded = PremiumTrackTwitchAccounts.select {
					PremiumTrackTwitchAccounts.guildId eq guild.idLong and (PremiumTrackTwitchAccounts.twitchUserId eq twitchUserId)
				}.count() == 1L

				if (!isAlreadyAdded) {
					// We don't reeally care if there's already a premium track inserted
					val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
						.toList()
						.sumOf { it.value }
						.let { ceil(it) }

					val plan = ServerPremiumPlans.getPlanFromValue(valueOfTheDonationKeysEnabledOnThisGuild)

					val premiumTracksOfTheGuildCount =
						PremiumTrackTwitchAccounts.slice(PremiumTrackTwitchAccounts.twitchUserId).select {
							PremiumTrackTwitchAccounts.guildId eq guild.idLong
						}.orderBy(
							PremiumTrackTwitchAccounts.addedAt,
							SortOrder.ASC
						) // Ordered by the added at date...
							.count()

					if (premiumTracksOfTheGuildCount >= plan.maxUnauthorizedTwitchChannels)
						return@transaction AddGuildTwitchChannelResult.TooManyPremiumTracks

					PremiumTrackTwitchAccounts.insert {
						it[PremiumTrackTwitchAccounts.guildId] = guild.idLong
						it[PremiumTrackTwitchAccounts.twitchUserId] = twitchUserId
						it[PremiumTrackTwitchAccounts.addedBy] = userIdentification.id.toLong()
						it[PremiumTrackTwitchAccounts.addedAt] = Instant.now()
					}
				}
			}

			// Does not exist, so let's insert it!
			val trackId = TrackedTwitchAccounts.insertAndGetId {
				it[TrackedTwitchAccounts.guildId] = guild.idLong
				it[TrackedTwitchAccounts.channelId] = channelId
				it[TrackedTwitchAccounts.twitchUserId] = twitchUserId
				it[TrackedTwitchAccounts.message] = message
			}

			val state = TwitchWebUtils.getTwitchAccountTrackState(twitchUserId)

			return@transaction AddGuildTwitchChannelResult.Success(trackId.value, state)
		}

		when (result) {
			is AddGuildTwitchChannelResult.Success -> {
				val twitchUser = TwitchWebUtils.getCachedUsersInfoById(loritta, twitchUserId)
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
						result.trackId,
						createPremiumTrack,
						twitchUser,
						result.state
					).generateHtml()
				)
			}
			AddGuildTwitchChannelResult.TooManyPremiumTracks -> TODO()
		}
	}

	sealed class AddGuildTwitchChannelResult {
		@Serializable
		class Success(val trackId: Long, val state: TwitchAccountTrackState) : AddGuildTwitchChannelResult()

		@Serializable
		data object TooManyPremiumTracks : AddGuildTwitchChannelResult()
	}
}