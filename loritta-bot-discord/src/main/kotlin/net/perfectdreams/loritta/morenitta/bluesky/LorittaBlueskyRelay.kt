package net.perfectdreams.loritta.morenitta.bluesky

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedBlueskyAccounts
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.analytics.LorittaMetrics
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.execute
import net.perfectdreams.loritta.morenitta.rpc.payloads.BlueskyPostRelayRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.BlueskyPostRelayResponse
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks.PostTwitchEventSubCallbackRoute
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import net.perfectdreams.yokye.BlueskyFirehoseClient
import org.jetbrains.exposed.sql.selectAll
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.seconds

class LorittaBlueskyRelay(val loritta: LorittaBot) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    val firehoseClient = BlueskyFirehoseClient()
    val postStream = firehoseClient.postStream
    private val postsReceivedGauge = LorittaMetrics.appMicrometerRegistry.gauge("loritta.bluesky.posts_received", AtomicLong(0))

    suspend fun startRelay() {
        firehoseClient.connect()

        while (true) {
            try {
                // We do have a minimum post count to avoid hammering the database, because there's a LOT of bsky posts
                // It isn't a huge deal if we don't fill 100 posts, because we will wait for 1s before bailing out and continuing anyway
                val posts = postStream.receiveAll(minimum = 100, maximumQueryTimeout = 1.seconds)
                val postsToBeRelayed = mutableListOf<BlueskyPostRelayRequest>()

                // We chunk due to limits on IN queries
                posts.chunked(65_535)
                    .forEach { chunkPosts ->
                        postsReceivedGauge.incrementAndGet()
                        val repos = chunkPosts.map { it.repo }

                        // To avoid spamming all Loritta instances with useless posts, we'll do a "pre-filter" here to know if we need to relay something or not
                        val trackedBlueskyAccounts = loritta.transaction {
                            TrackedBlueskyAccounts.selectAll()
                                .where {
                                    TrackedBlueskyAccounts.repo inList repos
                                }
                                .toList()
                        }

                        if (trackedBlueskyAccounts.isNotEmpty()) {
                            // So we are tracking someone, sweet!
                            for (post in chunkPosts) {
                                val tracks = mutableListOf<BlueskyPostRelayRequest.TrackInfo>()

                                val relaysInfo = trackedBlueskyAccounts.filter { it[TrackedBlueskyAccounts.repo] == post.repo }

                                for (relayInfo in relaysInfo) {
                                    // Add all tracks info
                                    tracks.add(
                                        BlueskyPostRelayRequest.TrackInfo(
                                            relayInfo[TrackedBlueskyAccounts.guildId],
                                            relayInfo[TrackedBlueskyAccounts.channelId],
                                            relayInfo[TrackedBlueskyAccounts.message]
                                        )
                                    )
                                }

                                if (tracks.isNotEmpty()) {
                                    postsToBeRelayed.add(
                                        BlueskyPostRelayRequest(
                                            post.repo,
                                            post.postId,
                                            tracks
                                        )
                                    )
                                }
                            }
                        }
                    }

                // And now, finally, we can relay them!
                for (postToBeRelayed in postsToBeRelayed) {
                    val clustersThatThisPostNeedsToBeRelayedTo = loritta.config.loritta.clusters.instances
                        .filter { cluster ->
                            postToBeRelayed.tracks.any { track ->
                                DiscordUtils.getLorittaClusterForGuildId(loritta, track.guildId) == cluster
                            }
                        }

                    val jobs = clustersThatThisPostNeedsToBeRelayedTo.map { cluster ->
                        cluster to GlobalScope.async {
                            withTimeout(25_000) {
                                LorittaRPC.BlueskyPostRelay.execute(
                                    loritta,
                                    cluster,
                                    postToBeRelayed
                                )
                            }
                        }
                    }

                    var totalNotifiedCount = 0
                    for (job in jobs) {
                        try {
                            val relayResult = job.second.await()
                            if (relayResult is BlueskyPostRelayResponse.Success) {
                                logger.info { "Bluesky Post Relay of ${postToBeRelayed.postId} by ${postToBeRelayed.repo} to Cluster ${job.first.id} (${job.first.name}) was successfully processed! Notified Guilds: ${relayResult.notifiedGuilds.size}" }
                                totalNotifiedCount += relayResult.notifiedGuilds.size
                            } else {
                                error("Relay result is not Success! Result: $relayResult")
                            }
                        } catch (e: Exception) {
                            logger.warn(e) { "Bluesky Post Relay of ${postToBeRelayed.postId} by ${postToBeRelayed.repo} to Cluster ${job.first.id} (${job.first.name}) failed!" }
                        }
                    }
                    logger.info { "Bluesky Post Relay of ${postToBeRelayed.postId} by ${postToBeRelayed.repo} completed! Notification Count: $totalNotifiedCount" }
                }
            } catch (e: Exception) {
                logger.warn(e) { "Something went wrong while processing post!" }
            }
        }
    }
}