package net.perfectdreams.loritta.morenitta.utils

import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Activity.ActivityType
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.tables.GatewayActivities
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.time.Instant

/**
 * Creates and updates gateway activities that are stored on the database
 */
class ActivityUpdater(val loritta: LorittaBot) : RunnableCoroutine {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private var lastActivity: ActivityWrapper? = null

    suspend fun loadActivity(): ActivityWrapper? {
        // Create a new activity
        val now = Instant.now()
        val gatewayActivity = loritta.newSuspendedTransaction {
            GatewayActivities.select {
                GatewayActivities.startsAt lessEq Instant.now() and (GatewayActivities.endsAt greaterEq now)
            }.orderBy(Pair(GatewayActivities.startsAt, SortOrder.DESC), Pair(GatewayActivities.priority, SortOrder.DESC))
                .limit(1)
                .firstOrNull()
        } ?: return null

        val text = gatewayActivity[GatewayActivities.text]
        val type = ActivityType.valueOf(gatewayActivity[GatewayActivities.type])
        val streamUrl = gatewayActivity[GatewayActivities.streamUrl]

        return ActivityWrapper(
            text,
            type,
            streamUrl
        )
    }

    override suspend fun run() {
        logger.info { "Loading and updating activity..." }
        // Load the stored activity
        val newActivity = loadActivity()

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
                else -> error("I don't know how to handle ${type}!")
            }
        }
    }
}