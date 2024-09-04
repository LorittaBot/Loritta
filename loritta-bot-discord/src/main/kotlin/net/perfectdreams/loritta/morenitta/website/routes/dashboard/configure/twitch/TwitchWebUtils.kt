package net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.twitch

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.tables.CachedTwitchChannels
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.AlwaysTrackTwitchAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.AuthorizedTwitchAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.PremiumTrackTwitchAccounts
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchUpsert
import org.jetbrains.exposed.sql.select
import java.time.Duration
import java.time.Instant
import kotlin.math.ceil

object TwitchWebUtils {
    suspend fun getCachedUsersInfoById(loritta: LorittaBot, vararg ids: Long): List<net.perfectdreams.switchtwitch.data.TwitchUser> {
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

    suspend fun getCachedUsersInfoByLogin(loritta: LorittaBot, vararg logins: String): List<net.perfectdreams.switchtwitch.data.TwitchUser> {
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

    fun getTwitchAccountTrackState(twitchUserId: Long): TwitchAccountTrackState {
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