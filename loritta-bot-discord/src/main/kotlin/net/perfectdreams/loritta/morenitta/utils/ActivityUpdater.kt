package net.perfectdreams.loritta.morenitta.utils

import dev.minn.jda.ktx.coroutines.await
import io.ktor.client.request.*
import io.ktor.client.statement.*
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Activity.ActivityType
import net.dv8tion.jda.api.entities.Icon
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.tables.FanArtsExtravaganza
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * Creates and updates gateway activities that are stored on the database
 */
class ActivityUpdater(val loritta: LorittaBot) : RunnableCoroutine {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private var lastActivity: ActivityWrapper? = null
    private var lastFanArtUpdate = 0L
    private var fanArtIndex = 0

    override suspend fun run() {
        logger.info { "Loading and updating activity..." }

        val now = LocalDateTime.now(Constants.LORITTA_TIMEZONE)

        // Let's develop this as if Loritta only had a single instance, shall we?
        if (loritta.isMainInstance) {
            val firstShard = loritta.lorittaShards.getShards().firstOrNull { it.status == JDA.Status.CONNECTED }

            if (firstShard != null) {
                // We are going to do this in two passes: Once to update the avatars, and another to update the status
                // Only the first cluster should manage avatar changes and fan art updates

                // Update the current active avatar
                // Is today a Fan Art Extravaganza day?
                if (now.dayOfWeek == DayOfWeek.SUNDAY) {
                    // Fan Art Extravaganza day
                    val diff = System.currentTimeMillis() - lastFanArtUpdate

                    if (diff >= 600_000L) { // only need to update every 10 minutes
                        logger.info { "Updating current fan art... Current fan art index is $fanArtIndex" }

                        // Get enabled fan arts from the database
                        val fanArts = loritta.transaction {
                            FanArtsExtravaganza.select {
                                FanArtsExtravaganza.enabled eq true and (FanArtsExtravaganza.defaultAvatar eq false)
                            }.toList()
                        }

                        if (fanArts.isNotEmpty()) {
                            if (fanArtIndex >= fanArts.size)
                                fanArtIndex = 0 // Reset

                            val newFanArtRR = fanArts[fanArtIndex] // Should NEVER be null!

                            // Only update if it ain't active (in THEORY it should always be non-active... but oh well, I guess the avatar could be set to active if Loritta restarts)
                            if (!newFanArtRR[FanArtsExtravaganza.active]) {
                                // Update Loritta's avatar
                                logger.info { "Updating Loritta's fan art avatar to ${newFanArtRR[FanArtsExtravaganza.fanArtAvatarImageUrl]}" }
                                val fanArtImageData =
                                    loritta.http.get(newFanArtRR[FanArtsExtravaganza.fanArtAvatarImageUrl])
                                        .readBytes()

                                firstShard.selfUser.manager.setAvatar(Icon.from(fanArtImageData)).await()

                                logger.info { "Updated Loritta's fan art avatar!" }

                                // Set the new fan art avatar as active
                                loritta.transaction {
                                    // Set all to false
                                    FanArtsExtravaganza.update {
                                        it[FanArtsExtravaganza.active] = false
                                    }

                                    // Set only the default avatar to true
                                    FanArtsExtravaganza.update({
                                        FanArtsExtravaganza.id eq newFanArtRR[FanArtsExtravaganza.id]
                                    }) {
                                        it[FanArtsExtravaganza.active] = true
                                    }
                                }
                            } else {
                                logger.info { "Not updating Loritta's Fan Art Avatar because the current avatar is already active..." }
                            }

                            // Update last update time
                            this.lastFanArtUpdate = System.currentTimeMillis()

                            // And increase the index for the next update
                            this.fanArtIndex++
                        } else {
                            logger.warn { "No fan arts are present for the Fan Art Extravaganza! Skipping fan art update..." }
                        }
                    }
                } else {
                    // Set the default avatar as active, but only if the current avatar is not active
                    val defaultAvatarRR = loritta.transaction {
                        FanArtsExtravaganza.select {
                            FanArtsExtravaganza.defaultAvatar eq true and (FanArtsExtravaganza.enabled eq true)
                        }.firstOrNull()
                    }

                    // Only update if it ain't active
                    if (defaultAvatarRR != null) {
                        if (!defaultAvatarRR[FanArtsExtravaganza.active]) {
                            logger.info { "Updating Loritta's default avatar to ${defaultAvatarRR[FanArtsExtravaganza.fanArtAvatarImageUrl]}" }
                            val defaultAvatarImageData =
                                loritta.http.get(defaultAvatarRR[FanArtsExtravaganza.fanArtAvatarImageUrl])
                                    .readBytes()

                            firstShard.selfUser.manager.setAvatar(Icon.from(defaultAvatarImageData)).await()

                            logger.info { "Updated Loritta's default avatar!" }

                            loritta.transaction {
                                // Set all to false
                                FanArtsExtravaganza.update {
                                    it[FanArtsExtravaganza.active] = false
                                }

                                // Set only the default avatar to true
                                FanArtsExtravaganza.update({
                                    FanArtsExtravaganza.defaultAvatar eq true and (FanArtsExtravaganza.enabled eq true)
                                }) {
                                    it[FanArtsExtravaganza.active] = true
                                }
                            }
                        } else {
                            logger.info { "Not updating Loritta's Default Avatar because the current avatar is already active..." }
                        }
                    } else {
                        logger.warn { "Default Loritta Avatar could not be found..." }
                    }
                }
            } else {
                logger.warn { "Couldn't get a single connected shard! Ignoring Loritta's avatar update..." }
            }
        }

        // The "update activity" function does query if it has a fan art avatar set
        updateActivity()
    }

    private suspend fun updateActivity() {
        // Load the stored activity
        val newActivity = loritta.loadActivity()

        setActivity(newActivity)
    }

    private fun setActivity(newActivity: ActivityWrapper?) {
        logger.info { "New activity is $newActivity" }

        // Calling setActivityProvider does update the current activity of all shards
        if (newActivity == null) {
            if (lastActivity != null) {
                logger.info { "Setting new activity provider to null" }
                lastActivity = null
                loritta.lorittaShards.shardManager.setActivityProvider {
                    null
                }
            }
        } else {
            if (lastActivity != newActivity) {
                logger.info { "Setting new activity provider to $newActivity" }
                this.lastActivity = newActivity

                loritta.lorittaShards.shardManager.setActivityProvider {
                    newActivity.convertToJDAActivity(loritta, it)
                }
            }
        }
    }

    data class ActivityWrapper(
        val text: String,
        val type: ActivityType,
        val streamUrl: String?
    ) {
        fun convertToJDAActivity(loritta: LorittaBot, shardId: Int): Activity {
            val activityText = loritta.createActivityText(text, shardId)

            return when (type) {
                ActivityType.PLAYING -> Activity.playing(activityText)
                ActivityType.LISTENING -> Activity.listening(activityText)
                ActivityType.COMPETING -> Activity.competing(activityText)
                ActivityType.WATCHING -> Activity.watching(activityText)
                ActivityType.STREAMING -> Activity.streaming(activityText, streamUrl)
                ActivityType.CUSTOM_STATUS -> Activity.customStatus(activityText)
                else -> error("I don't know how to handle ${type}!")
            }
        }
    }
}