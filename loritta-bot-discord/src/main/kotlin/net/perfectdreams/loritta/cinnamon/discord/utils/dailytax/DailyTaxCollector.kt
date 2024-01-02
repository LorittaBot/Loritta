package net.perfectdreams.loritta.cinnamon.discord.utils.dailytax

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyTaxNotifiedUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxTaxedUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxWarnUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.UserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.DailyTaxSonhosTransactionsLog
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.sql.Connection
import java.time.LocalDateTime
import java.time.ZoneOffset

class DailyTaxCollector(val m: LorittaBot) : RunnableCoroutine {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun run() {
        // TODO: proper i18n
        val i18nContext = m.languageManager.getI18nContextById("pt")

        try {
            logger.info { "Collecting tax from inactive daily users..." }

            val now = Clock.System.now()
            val nextTrigger = LocalDateTime.now(ZoneOffset.UTC)
                .plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .toInstant(ZoneOffset.UTC)
                .toKotlinInstant()

            val alreadyWarnedThatTheyWereTaxed = mutableSetOf<Long>()
            val alreadyWarnedThatTheyAreGoingToBeTaxed = mutableSetOf<Long>()

            // We need to use Read Commited to avoid "Could not serialize access due to concurrent update"
            // This is more "unsafe" because we may make someone be in the negative sonhos, but there isn't another good alterative, so yeah...
            m.pudding.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
                val notifiedUsers = DailyTaxNotifiedUsers.slice(DailyTaxNotifiedUsers.user).selectAll().map { it[DailyTaxNotifiedUsers.user].value }.toSet()

                DailyTaxUtils.getAndProcessInactiveDailyUsers(m.config.loritta.discord.applicationId, 0) { threshold, inactiveDailyUser ->
                    if (notifiedUsers.contains(inactiveDailyUser.id)) {
                        logger.info { "Adding important notification to ${inactiveDailyUser.id} about daily tax taxed" }

                        alreadyWarnedThatTheyWereTaxed.add(inactiveDailyUser.id)

                        Profiles.update({ Profiles.id eq inactiveDailyUser.id }) {
                            with(SqlExpressionBuilder) {
                                it.update(Profiles.money, Profiles.money - inactiveDailyUser.moneyToBeRemoved)
                            }
                        }

                        val timestampLogId = SonhosTransactionsLog.insertAndGetId {
                            it[SonhosTransactionsLog.user] = inactiveDailyUser.id
                            it[SonhosTransactionsLog.timestamp] = now.toJavaInstant()
                        }

                        DailyTaxSonhosTransactionsLog.insert {
                            it[DailyTaxSonhosTransactionsLog.timestampLog] = timestampLogId
                            it[DailyTaxSonhosTransactionsLog.sonhos] = inactiveDailyUser.moneyToBeRemoved
                            it[DailyTaxSonhosTransactionsLog.maxDayThreshold] = threshold.maxDayThreshold
                            it[DailyTaxSonhosTransactionsLog.minimumSonhosForTrigger] = threshold.minimumSonhosForTrigger
                            it[DailyTaxSonhosTransactionsLog.tax] = threshold.tax
                        }

                        val userNotificationId = UserNotifications.insertAndGetId {
                            it[UserNotifications.timestamp] = now.toJavaInstant()
                            it[UserNotifications.user] = inactiveDailyUser.id
                        }

                        DailyTaxTaxedUserNotifications.insert {
                            it[DailyTaxTaxedUserNotifications.timestampLog] = userNotificationId
                            it[DailyTaxTaxedUserNotifications.nextInactivityTaxTimeWillBeTriggeredAt] = nextTrigger.toJavaInstant()
                            it[DailyTaxTaxedUserNotifications.currentSonhos] = inactiveDailyUser.money
                            it[DailyTaxTaxedUserNotifications.howMuchWasRemoved] = inactiveDailyUser.moneyToBeRemoved
                            it[DailyTaxTaxedUserNotifications.maxDayThreshold] = threshold.maxDayThreshold
                            it[DailyTaxTaxedUserNotifications.minimumSonhosForTrigger] = threshold.minimumSonhosForTrigger
                            it[DailyTaxTaxedUserNotifications.tax] = threshold.tax
                        }

                        // Remove it so the next time they are taxed, they are warned again
                        DailyTaxNotifiedUsers.deleteWhere {
                            DailyTaxNotifiedUsers.user eq inactiveDailyUser.id
                        }

                        DailyTaxUtils.insertImportantNotification(
                            inactiveDailyUser,
                            userNotificationId.value
                        )
                    } else {
                        logger.info { "Skipping ${inactiveDailyUser.id} daily tax taxed because they weren't warned before..." }
                    }
                }
            }

            logger.info { "Successfully collected today's daily tax!" }

            suspend fun warnTaxInTheFutureDays(dayOffset: Long) {
                logger.info { "Checking how many users would be affected if they didn't get the daily between today and +$dayOffset days..." }
                val plusXDaysAtMidnight = LocalDateTime.now(ZoneOffset.UTC)
                    .plusDays(dayOffset)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0)
                    .toInstant(ZoneOffset.UTC)
                    .toKotlinInstant()

                m.pudding.transaction {
                    DailyTaxUtils.getAndProcessInactiveDailyUsers(m.config.loritta.discord.applicationId, 1) { threshold, inactiveDailyUser ->
                        // Don't warn them about the tax if they were already taxed before
                        if (inactiveDailyUser.id !in alreadyWarnedThatTheyWereTaxed && inactiveDailyUser.id !in alreadyWarnedThatTheyAreGoingToBeTaxed) {
                            logger.info { "Adding important notification to ${inactiveDailyUser.id} about daily tax warn" }

                            val userNotificationId = UserNotifications.insertAndGetId {
                                it[UserNotifications.timestamp] = now.toJavaInstant()
                                it[UserNotifications.user] = inactiveDailyUser.id
                            }

                            DailyTaxWarnUserNotifications.insert {
                                it[DailyTaxWarnUserNotifications.timestampLog] = userNotificationId
                                it[DailyTaxWarnUserNotifications.inactivityTaxTimeWillBeTriggeredAt] = plusXDaysAtMidnight.toJavaInstant()
                                it[DailyTaxWarnUserNotifications.currentSonhos] = inactiveDailyUser.money
                                it[DailyTaxWarnUserNotifications.howMuchWasRemoved] = inactiveDailyUser.moneyToBeRemoved
                                it[DailyTaxWarnUserNotifications.maxDayThreshold] = threshold.maxDayThreshold
                                it[DailyTaxWarnUserNotifications.minimumSonhosForTrigger] = threshold.minimumSonhosForTrigger
                                it[DailyTaxWarnUserNotifications.tax] = threshold.tax
                            }

                            // insert ignore: "allows insert statements to be executed without throwing any ignorable errors."
                            DailyTaxNotifiedUsers.insertIgnore {
                                it[DailyTaxNotifiedUsers.notifiedAt] = now.toJavaInstant()
                                it[DailyTaxNotifiedUsers.user] = inactiveDailyUser.id
                            }

                            DailyTaxUtils.insertImportantNotification(
                                inactiveDailyUser,
                                userNotificationId.value
                            )

                            alreadyWarnedThatTheyAreGoingToBeTaxed.add(inactiveDailyUser.id)
                        }
                    }
                }
            }

            warnTaxInTheFutureDays(1)
            warnTaxInTheFutureDays(2)
            warnTaxInTheFutureDays(3)
        } catch (e: Exception) {
            logger.warn { "Something went wrong while collecting tax from inactive daily users!" }
        }
    }
}