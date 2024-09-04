package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch

import io.ktor.server.application.*
import io.ktor.server.util.*
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.stream.createHTML
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.CachedTwitchChannels
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.AlwaysTrackTwitchAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.AuthorizedTwitchAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.PremiumTrackTwitchAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.RequiresGuildAuthLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.twitch.GuildTwitchView.Companion.createPremiumTwitchAccountCards
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.twitch.GuildTwitchView.Companion.createTwitchAccountCards
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.serializable.TwitchUser
import net.perfectdreams.loritta.serializable.config.GuildTwitchConfig
import net.perfectdreams.loritta.serializable.config.PremiumTrackTwitchAccount
import net.perfectdreams.loritta.serializable.config.TrackedTwitchAccount
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Duration
import java.time.Instant
import kotlin.math.ceil

class DeleteTwitchPremiumTrackRoute(loritta: LorittaBot) : RequiresGuildAuthLocalizedDashboardRoute(loritta, "/configure/twitch/premium-tracks/{trackId}") {
	override suspend fun onDashboardGuildAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, colorTheme: ColorTheme) {
		val trackId = call.parameters.getOrFail("trackId").toLong()

		val (twitchAccounts, premiumTrackTwitchAccounts, valueOfTheDonationKeysEnabledOnThisGuild) = loritta.newSuspendedTransaction {
			PremiumTrackTwitchAccounts.deleteWhere {
				PremiumTrackTwitchAccounts.id eq trackId and (PremiumTrackTwitchAccounts.guildId eq guild.idLong)
			}

			val twitchAccounts = TrackedTwitchAccounts.select { TrackedTwitchAccounts.guildId eq guild.idLong }
				.map {
					val state = getTwitchAccountTrackState(it[TrackedTwitchAccounts.twitchUserId])

					Pair(
						state,
						TrackedTwitchAccount(
							it[TrackedTwitchAccounts.id].value,
							it[TrackedTwitchAccounts.twitchUserId],
							it[TrackedTwitchAccounts.channelId],
							it[TrackedTwitchAccounts.message]
						)
					)
				}

			val premiumTrackTwitchAccounts = PremiumTrackTwitchAccounts.select {
				PremiumTrackTwitchAccounts.guildId eq guild.idLong
			}.map {
				PremiumTrackTwitchAccount(
					it[PremiumTrackTwitchAccounts.id].value,
					it[PremiumTrackTwitchAccounts.twitchUserId]
				)
			}

			val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
				.toList()
				.sumOf { it.value }
				.let { ceil(it) }

			Triple(twitchAccounts, premiumTrackTwitchAccounts, valueOfTheDonationKeysEnabledOnThisGuild)
		}

		val accountsInfo = getCachedUsersInfoById(
			*((twitchAccounts.map { it.second.twitchUserId } + premiumTrackTwitchAccounts.map { it.twitchUserId }).toSet()).toLongArray()
		)

		val twitchConfig = GuildTwitchConfig(
			twitchAccounts.map { trackedTwitchAccount ->
				GuildTwitchConfig.TrackedTwitchAccountWithTwitchUserAndTrackingState(
					trackedTwitchAccount.first,
					trackedTwitchAccount.second,
					accountsInfo.firstOrNull { it.id == trackedTwitchAccount.second.twitchUserId }?.let {
						TwitchUser(it.id, it.login, it.displayName, it.profileImageUrl)
					}
				)
			},
			premiumTrackTwitchAccounts.map { trackedTwitchAccount ->
				GuildTwitchConfig.PremiumTrackTwitchAccountWithTwitchUser(
					trackedTwitchAccount,
					accountsInfo.firstOrNull { it.id == trackedTwitchAccount.twitchUserId }?.let {
						TwitchUser(it.id, it.login, it.displayName, it.profileImageUrl)
					}
				)
			}
		)

		call.response.headerHXTrigger {
			closeSpicyModal = true
			playSoundEffect = "recycle-bin"
			spicyToast = EmbeddedSpicyModalUtils.createSpicyToast(EmbeddedSpicyToast.Type.SUCCESS, "Acompanhamento premium deletado!")
		}

		call.respondHtml(
			createHTML()
				.body {
					createPremiumTwitchAccountCards(loritta, i18nContext, guild, twitchConfig.premiumTrackTwitchAccounts)

					div {
						id = "tracked-twitch-accounts-wrapper"

						attributes["hx-swap-oob"] = "true"
						createTwitchAccountCards(loritta, i18nContext, guild, twitchConfig.trackedTwitchAccounts)
					}
				}
		)
	}

	private suspend fun getCachedUsersInfoById(vararg ids: Long): List<net.perfectdreams.switchtwitch.data.TwitchUser> {
		// bye
		if (ids.isEmpty())
			return emptyList()

		val now24HoursAgo = Instant.now().minus(Duration.ofHours(24))

		val twitchUsers = mutableListOf<net.perfectdreams.switchtwitch.data.TwitchUser>()
		val idsToBeQueried = ids.toMutableList()

		// Get from our cache first
		val results = loritta.transaction {
			CachedTwitchChannels.select {
				CachedTwitchChannels.id inList idsToBeQueried and (CachedTwitchChannels.queriedAt greaterEq now24HoursAgo)
			}.toList()
		}

		for (result in results) {
			val data = result[CachedTwitchChannels.data]
			// If the data is null, then it means that the channel does not exist!
			if (data != null) {
				twitchUsers.add(Json.decodeFromString(data))
			}

			idsToBeQueried.remove(result[CachedTwitchChannels.id].value)
		}

		if (idsToBeQueried.isEmpty())
			return twitchUsers

		// Query anyone that wasn't matched by our cache!
		val queriedUsers = loritta.switchTwitch.getUsersInfoById(*idsToBeQueried.toLongArray())

		// And add to our cache
		if (queriedUsers.isNotEmpty()) {
			loritta.transaction {
				CachedTwitchChannels.batchUpsert(
					queriedUsers,
					CachedTwitchChannels.id,
					shouldReturnGeneratedValues = false
				) { item ->
					this[CachedTwitchChannels.id] = item.id
					this[CachedTwitchChannels.userLogin] = item.login
					this[CachedTwitchChannels.data] = Json.encodeToString(item)
					this[CachedTwitchChannels.queriedAt] = Instant.now()
				}
			}
		}

		twitchUsers += queriedUsers

		return twitchUsers
	}

	private suspend fun getCachedUsersInfoByLogin(vararg logins: String): List<net.perfectdreams.switchtwitch.data.TwitchUser> {
		// bye
		if (logins.isEmpty())
			return emptyList()

		val now24HoursAgo = Instant.now().minus(Duration.ofHours(24))

		val twitchUsers = mutableListOf<net.perfectdreams.switchtwitch.data.TwitchUser>()
		val idsToBeQueried = logins.toMutableList()

		// Get from our cache first
		val results = loritta.transaction {
			CachedTwitchChannels.select {
				CachedTwitchChannels.userLogin inList idsToBeQueried and (CachedTwitchChannels.queriedAt greaterEq now24HoursAgo)
			}.toList()
		}

		for (result in results) {
			val data = result[CachedTwitchChannels.data]
			// If the data is null, then it means that the channel does not exist!
			if (data != null) {
				twitchUsers.add(Json.decodeFromString(data))
			}

			idsToBeQueried.remove(result[CachedTwitchChannels.userLogin])
		}

		if (idsToBeQueried.isEmpty())
			return twitchUsers

		// Query anyone that wasn't matched by our cache!
		val queriedUsers = loritta.switchTwitch.getUsersInfoByLogin(*idsToBeQueried.toTypedArray())

		// And add to our cache
		if (queriedUsers.isNotEmpty()) {
			loritta.transaction {
				CachedTwitchChannels.batchUpsert(
					queriedUsers,
					CachedTwitchChannels.id,
					shouldReturnGeneratedValues = false
				) { item ->
					this[CachedTwitchChannels.id] = item.id
					this[CachedTwitchChannels.userLogin] = item.login
					this[CachedTwitchChannels.data] = Json.encodeToString(item)
					this[CachedTwitchChannels.queriedAt] = Instant.now()
				}
			}
		}

		twitchUsers += queriedUsers

		return twitchUsers
	}

	private fun getTwitchAccountTrackState(twitchUserId: Long): TwitchAccountTrackState {
		val isAuthorized = AuthorizedTwitchAccounts.select {
			AuthorizedTwitchAccounts.userId eq twitchUserId
		}.count() == 1L

		if (isAuthorized)
			return TwitchAccountTrackState.AUTHORIZED

		val isAlwaysTrack = AlwaysTrackTwitchAccounts.select {
			AlwaysTrackTwitchAccounts.userId eq twitchUserId
		}.count() == 1L

		if (isAlwaysTrack)
			return TwitchAccountTrackState.ALWAYS_TRACK_USER

		// Get if the premium track is enabled for this account, we need to check if any of the servers has a premium key enabled too
		val guildIds = PremiumTrackTwitchAccounts.slice(PremiumTrackTwitchAccounts.guildId).select {
			PremiumTrackTwitchAccounts.twitchUserId eq twitchUserId
		}.toList().map { it[PremiumTrackTwitchAccounts.guildId] }

		for (guildId in guildIds) {
			// This is a bit tricky to check, since we need to check what kind of plan the user has
			val valueOfTheDonationKeysEnabledOnThisGuild = DonationKey.find { DonationKeys.activeIn eq guildId and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
				.toList()
				.sumOf { it.value }
				.let { ceil(it) }

			val plan = ServerPremiumPlans.getPlanFromValue(valueOfTheDonationKeysEnabledOnThisGuild)

			if (plan.maxUnauthorizedTwitchChannels != 0) {
				// If the plan has a maxUnauthorizedTwitchChannels != 0, now we need to get ALL premium tracks of the guild...
				val allPremiumTracksOfTheGuild = PremiumTrackTwitchAccounts.slice(PremiumTrackTwitchAccounts.twitchUserId).select {
					PremiumTrackTwitchAccounts.guildId eq guildId
				}.orderBy(PremiumTrackTwitchAccounts.addedAt, SortOrder.ASC) // Ordered by the added at date...
					.limit(plan.maxUnauthorizedTwitchChannels) // Limited by the max unauthorized count...
					.map { it[PremiumTrackTwitchAccounts.twitchUserId] } // Then we map by the twitch user ID...

				// And now, if the twitch User ID is in the list, then it means that...
				// 1. The guild is premium
				// 2. Has the user ID in the premium track
				// 3. And the plan fits the amount of premium tracks the user has
				if (twitchUserId in allPremiumTracksOfTheGuild)
					return TwitchAccountTrackState.PREMIUM_TRACK_USER
			}
		}

		return TwitchAccountTrackState.UNAUTHORIZED
	}
}