package net.perfectdreams.loritta.cinnamon.discord.utils.dailytax

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxWarnUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.UserNotifications
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import java.sql.Connection
import java.time.LocalDateTime
import java.time.ZoneOffset

class DailyTaxWarner(val m: LorittaBot) : RunnableCoroutine {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun run() {
        // TODO: proper i18n
        val i18nContext = m.languageManager.getI18nContextById("pt")

        try {
            logger.info { "Checking if someone would lose sonhos to today's daily tax..." }

            val now = Clock.System.now()

            val tomorrowAtMidnight = LocalDateTime.now(ZoneOffset.UTC)
                .plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .toInstant(ZoneOffset.UTC)
                .toKotlinInstant()

            // We need to use Read Commited to avoid "Could not serialize access due to concurrent update"
            // This is more "unsafe" because we may make someone be in the negative sonhos, but there isn't another good alterative, so yeah...
            m.pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
                DailyTaxUtils.getAndProcessInactiveDailyUsers(m.config.loritta.discord.applicationId, 0) { threshold, inactiveDailyUser ->
                    logger.info { "Adding important notification to ${inactiveDailyUser.id} about daily tax warn" }

                    val userNotificationId = UserNotifications.insertAndGetId {
                        it[UserNotifications.timestamp] = now.toJavaInstant()
                        it[UserNotifications.user] = inactiveDailyUser.id
                    }

                    DailyTaxWarnUserNotifications.insert {
                        it[DailyTaxWarnUserNotifications.timestampLog] = userNotificationId
                        it[DailyTaxWarnUserNotifications.inactivityTaxTimeWillBeTriggeredAt] = tomorrowAtMidnight.toJavaInstant()
                        it[DailyTaxWarnUserNotifications.currentSonhos] = inactiveDailyUser.money
                        it[DailyTaxWarnUserNotifications.howMuchWasRemoved] = inactiveDailyUser.moneyToBeRemoved
                        it[DailyTaxWarnUserNotifications.maxDayThreshold] = threshold.maxDayThreshold
                        it[DailyTaxWarnUserNotifications.minimumSonhosForTrigger] = threshold.minimumSonhosForTrigger
                        it[DailyTaxWarnUserNotifications.tax] = threshold.tax
                    }

                    DailyTaxUtils.insertImportantNotification(
                        inactiveDailyUser,
                        userNotificationId.value
                    )
                }
            }

            logger.info { "Successfully warned everyone about today's daily tax!" }
        } catch (e: Exception) {
            logger.warn { "Something went wrong while collecting and sending stats data!" }
        }
    }
}