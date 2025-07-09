package net.perfectdreams.loritta.morenitta.dailies

import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyReminderNotifications
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.scheduledtasks.NamedRunnableCoroutine
import net.perfectdreams.loritta.morenitta.utils.Constants
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import java.time.Instant
import java.time.ZonedDateTime

class DailyReminderTask(val m: LorittaBot) : NamedRunnableCoroutine {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    override val taskName = "daily-reminder-task"

    override suspend fun run() {
        val today = ZonedDateTime.now(Constants.LORITTA_TIMEZONE)
        val todayAtMidnight = today
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        val todayOneDayAgo = today
            .minusDays(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)

        val todayAtMidnightAsEpochMillis = todayAtMidnight.toInstant().toEpochMilli()
        val todayOneDayAgoAsEpochMillis = todayOneDayAgo.toInstant().toEpochMilli()

        m.pudding.transaction(repetitions = Int.MAX_VALUE) {
            // Update the task timer
            updateStoredTimer(m)

            val usersThatGotDailyYesterday = Dailies.select(Dailies.receivedById)
                .where {
                    Dailies.receivedAt greaterEq todayOneDayAgoAsEpochMillis and (Dailies.receivedAt.less(todayAtMidnightAsEpochMillis))
                }
                .map { it[Dailies.receivedById] }
                .distinct() // This technically is not needed BUT who knows right?

            logger.info { "There are ${usersThatGotDailyYesterday.size} users that will be reminded about their daily reward!" }

            // Really doesn't matter
            val now = Instant.now()

            DailyReminderNotifications.batchInsert(usersThatGotDailyYesterday) {
                this[DailyReminderNotifications.userId] = it
                this[DailyReminderNotifications.submittedAt] = now
                this[DailyReminderNotifications.triggeredForDaily] = todayAtMidnight.toInstant() // However this DOES matter!
                this[DailyReminderNotifications.processedAt] = null
                this[DailyReminderNotifications.successfullySent] = false
            }

            updateStoredTimer(m)
        }

        // Now everything else will be handled by another task :3 bye bye folks!
    }
}
