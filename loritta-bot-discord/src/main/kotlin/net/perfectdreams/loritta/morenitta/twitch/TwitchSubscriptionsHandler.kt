package net.perfectdreams.loritta.morenitta.twitch

import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.Clock
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.AlwaysTrackTwitchAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.AuthorizedTwitchAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.PremiumTrackTwitchAccounts
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedTwitchAccounts
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.DonationKey
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.switchtwitch.data.SubTransportCreate
import net.perfectdreams.switchtwitch.data.SubscriptionCreateRequest
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import kotlin.math.ceil

/**
 * Handles Twitch webhook subscriptions creation
 */
class TwitchSubscriptionsHandler(val m: LorittaBot) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val VALID_STATUS = setOf(
            "enabled",
            "webhook_callback_verification_pending"
        )
    }

    // This will be used to...
    // callTask() = Task is ran
    // callTask() = Task is queued
    // callTask() = Task is ignored because there is already a task being ran
    // callTask() = Task is ignored because there is already a task being ran
    // callTask() = Task is ignored because there is already a task being ran
    private val channel = Channel<String>(Channel.CONFLATED)

    /**
     * Requests the subscription task to be run
     */
    suspend fun requestSubscriptionCreation(reason: String) {
        logger.info { "Requesting Twitch subscription creation... Reason: $reason" }
        channel.send(reason)
    }

    suspend fun createSubscriptionsLoop() {
        for (reason in channel) {
            logger.info { "Received Twitch subscription creation ticket! Reason: $reason" }
            createSubscriptions(reason)
            logger.info { "Waiting for Twitch subscription creation ticket... Last reason was: $reason" }
        }
    }

    private suspend fun createSubscriptions(reason: String) {
        logger.info { "Creating Twitch subscriptions... Reason: $reason" }

        try {
            val start = Clock.System.now()

            // Get all tracked account data
            val trackedAccounts = m.pudding.transaction {
                TrackedTwitchAccounts.slice(TrackedTwitchAccounts.twitchUserId)
                    .selectAll()
                    .groupBy(TrackedTwitchAccounts.twitchUserId)
                    .toList()
            }

            // Get all subscriptions
            val createdSubscriptions = m.switchTwitch.loadAllSubscriptions()

            val streamersCurrentlyBeingTracked = mutableSetOf<Long>()
            val costlyStreams = mutableSetOf<Long>()
            val twitchUserIdToSubId = mutableMapOf<Long, String>()

            createdSubscriptions.forEach {
                it.data.forEach {
                    if (it.type == "stream.online") {
                        val broadcasterUserId = it.condition["broadcaster_user_id"]!!.toLong()
                        twitchUserIdToSubId[broadcasterUserId] = it.id
                        if (it.status in VALID_STATUS)
                            streamersCurrentlyBeingTracked.add(broadcasterUserId)
                        if (it.cost != 0)
                            costlyStreams.add(broadcasterUserId)
                    }
                }
            }

            // Get all subscriptions
            val allTwitchUserIds = trackedAccounts.map { it[TrackedTwitchAccounts.twitchUserId] }

            // We retrieve all user IDs beforehand to avoid unnecessary roundtrips to the database
            val (allAuthorizedUserIds, allAlwaysTrackUserIds, allPremiumTrackUserIdsAndGuildIds) = m.transaction {
                val allAuthorizedUserIds = AuthorizedTwitchAccounts.slice(AuthorizedTwitchAccounts.userId).selectAll()
                    .map { it[AuthorizedTwitchAccounts.userId] }

                val allAlwaysTrackUserIds =
                    AlwaysTrackTwitchAccounts.slice(AlwaysTrackTwitchAccounts.userId).selectAll()
                        .map { it[AlwaysTrackTwitchAccounts.userId] }

                val allPremiumTrackUserIdsAndGuildIds = PremiumTrackTwitchAccounts.slice(
                    PremiumTrackTwitchAccounts.twitchUserId,
                    PremiumTrackTwitchAccounts.guildId
                )
                    .selectAll()
                    .map {
                        Pair(it[PremiumTrackTwitchAccounts.twitchUserId], it[PremiumTrackTwitchAccounts.guildId])
                    }

                Triple(allAuthorizedUserIds, allAlwaysTrackUserIds, allPremiumTrackUserIdsAndGuildIds)
            }

            val allPremiumTrackUserIds = allPremiumTrackUserIdsAndGuildIds.map { it.first }

            logger.info { "Currently there are ${allTwitchUserIds.size} tracked Twitch user IDs on the database, ${streamersCurrentlyBeingTracked.size} subscriptions are registered on Twitch, ${allAuthorizedUserIds.size} are authorized, ${allAlwaysTrackUserIds.size} are always track and ${allPremiumTrackUserIdsAndGuildIds.size} are premium tracks" }

            for (twitchUserId in allTwitchUserIds) {
                // Some users are tracking this ID (bug?)
                if (twitchUserId == -1L) {
                    logger.info { "Skipping $twitchUserId because it is a invalid ID..." }
                    continue
                }

                // If the tracked stream is "costly", we need to check if the subscription should stay active
                if (streamersCurrentlyBeingTracked.contains(twitchUserId) && !costlyStreams.contains(twitchUserId)) {
                    logger.info { "Skipping $twitchUserId because they are already being tracked..." }
                    continue
                }

                // Let's check!
                val state = when (twitchUserId) {
                    in allAuthorizedUserIds -> TwitchAccountTrackState.AUTHORIZED
                    in allAlwaysTrackUserIds -> TwitchAccountTrackState.ALWAYS_TRACK_USER
                    in allPremiumTrackUserIds -> m.newSuspendedTransaction {
                        // Validate if the premium track is actually valid
                        // Get if the premium track is enabled for this account, we need to check if any of the servers has a premium key enabled too
                        val guildIds = PremiumTrackTwitchAccounts.slice(PremiumTrackTwitchAccounts.guildId).select {
                            PremiumTrackTwitchAccounts.twitchUserId eq twitchUserId
                        }.toList().map { it[PremiumTrackTwitchAccounts.guildId] }

                        for (guildId in guildIds) {
                            val valueOfTheDonationKeysEnabledOnThisGuild =
                                DonationKey.find { DonationKeys.activeIn eq guildId and (DonationKeys.expiresAt greaterEq System.currentTimeMillis()) }
                                    .toList()
                                    .sumOf { it.value }
                                    .let { ceil(it) }

                            if (valueOfTheDonationKeysEnabledOnThisGuild >= 40.0)
                                return@newSuspendedTransaction TwitchAccountTrackState.PREMIUM_TRACK_USER
                        }

                        return@newSuspendedTransaction TwitchAccountTrackState.UNAUTHORIZED
                    }

                    else -> TwitchAccountTrackState.UNAUTHORIZED
                }

                // If it is already being tracked...
                if (streamersCurrentlyBeingTracked.contains(twitchUserId)) {
                    if (state == TwitchAccountTrackState.AUTHORIZED && costlyStreams.contains(twitchUserId)) {
                        // If the state is authorized, but the cost is 1, then it means that the account authorization has been revoked!
                        logger.warn { "Subscription for $twitchUserId is costly even tho their state is $state! Revoking authorization state and deleting subscription..." }

                        m.pudding.transaction {
                            AuthorizedTwitchAccounts.deleteWhere {
                                AuthorizedTwitchAccounts.userId eq twitchUserId
                            }
                        }

                        // Delete the subscription!
                        m.switchTwitch.deleteSubscription(twitchUserIdToSubId[twitchUserId]!!)
                    }
                } else {
                    if (state != TwitchAccountTrackState.UNAUTHORIZED) {
                        logger.info { "Creating subscription for $twitchUserId because their state is $state..." }
                        // If the state ain't authorized, then we can create the subscription!
                        val subscriptionResult = m.switchTwitch.createSubscription(
                            SubscriptionCreateRequest(
                                "stream.online",
                                "1",
                                mapOf(
                                    "broadcaster_user_id" to twitchUserId.toString()
                                ),
                                SubTransportCreate(
                                    "webhook",
                                    m.config.loritta.twitch.webhookUrl,
                                    m.config.loritta.twitch.webhookSecret
                                )
                            )
                        )

                        val subscriptionData = subscriptionResult.data.firstOrNull()
                        // Honestly, I don't know when the sub result can return more than one data
                        if (subscriptionData != null) {
                            val cost = subscriptionData.cost
                            if (state == TwitchAccountTrackState.AUTHORIZED && cost != 0) {
                                logger.warn { "Subscription for $twitchUserId is costing $cost even tho their state is $state! Revoking authorization state and deleting subscription..." }

                                // If the state is authorized, but the cost is 0, then it means that the account authorization has been revoked!
                                m.pudding.transaction {
                                    AuthorizedTwitchAccounts.deleteWhere {
                                        AuthorizedTwitchAccounts.userId eq twitchUserId
                                    }
                                }

                                // Delete the subscription!
                                m.switchTwitch.deleteSubscription(subscriptionData.id)
                            }
                        }
                    } else if (costlyStreams.contains(twitchUserId)) {
                        // We don't log this because it ends up spamming the console way too much
                        // logger.info { "Skipping $twitchUserId because they are unauthorized..." }
                    }
                }
            }

            logger.info { "Finished processing Twitch subscriptions! Took ${Clock.System.now() - start}" }
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while processing Twitch subscriptions!" }
        }
    }
}