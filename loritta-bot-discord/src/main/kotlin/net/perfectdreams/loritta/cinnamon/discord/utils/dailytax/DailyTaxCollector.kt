package net.perfectdreams.loritta.cinnamon.discord.utils.dailytax

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyTaxNotifiedUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserLorittaAPITokens
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.DailyTaxTaxedUserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.notifications.UserNotifications
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.TokenType
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.serializable.StoredDailyTaxSonhosTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.sql.Connection
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class DailyTaxCollector(val m: LorittaBot) : RunnableCoroutine {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}

        /**
         * Queries the daily check bypass user IDs list
         *
         * @return a list containing all the IDs that should not be checked by Loritta
         */
        // This is in here because DailyTaxWarner also does the same checks
        fun queryDailyBypassList(lorittaBot: LorittaBot): List<Long> {
            val now = Instant.now()

            val bypassDailyTaxUserIds = mutableListOf<Long>()

            // lori so cute she doesn't deserve to be hit with the inactive daily tax
            bypassDailyTaxUserIds.add(lorittaBot.config.loritta.discord.applicationId)

            // Now our precious premium users
            val moneySum = Payments.money.sum()

            val cheapestPlanWithoutDailyInactivityTaxCost = UserPremiumPlans.getPlansThatDoNotHaveDailyInactivityTax()
                .minOf { it.cost }

            val usersToBeIgnored = Payments.select(Payments.userId, moneySum).where { 
                Payments.expiresAt greaterEq System.currentTimeMillis()
            }.groupBy(Payments.userId)
                .having { moneySum greaterEq (cheapestPlanWithoutDailyInactivityTaxCost - 10.00).toBigDecimal() } // It is actually 99.99 but shhhhh
                .map { it[Payments.userId] }
                .toMutableSet()

            bypassDailyTaxUserIds.addAll(usersToBeIgnored)

            val vacationUsers = Profiles.select(Profiles.id)
                .where {
                    Profiles.vacationUntil greaterEq now
                }
            bypassDailyTaxUserIds.addAll(vacationUsers.map { it[Profiles.id].value })

            // We expect that all BOT tokens are... well, bots! And they don't be taxed!!
            val botTokensIds = UserLorittaAPITokens.selectAll().where {
                UserLorittaAPITokens.tokenType eq TokenType.BOT
            }.map { it[UserLorittaAPITokens.tokenUserId] }

            bypassDailyTaxUserIds.addAll(botTokensIds)

            return bypassDailyTaxUserIds
        }
    }

    override suspend fun run() {
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
                val notifiedUsers = DailyTaxNotifiedUsers.select(DailyTaxNotifiedUsers.user).map { it[DailyTaxNotifiedUsers.user].value }.toSet()

                val bypassDailyTaxUserIds = queryDailyBypassList(m)

                DailyTaxUtils.getAndProcessInactiveDailyUsers(
                    bypassDailyTaxUserIds,
                    0
                ) { threshold, inactiveDailyUser ->
                    if (notifiedUsers.contains(inactiveDailyUser.id)) {
                        logger.info { "Adding important notification to ${inactiveDailyUser.id} about daily tax taxed" }

                        alreadyWarnedThatTheyWereTaxed.add(inactiveDailyUser.id)

                        Profiles.update({ Profiles.id eq inactiveDailyUser.id }) {
                            with(SqlExpressionBuilder) {
                                it.update(Profiles.money, Profiles.money - inactiveDailyUser.moneyToBeRemoved)
                            }
                        }

                        // Cinnamon transaction log
                        SimpleSonhosTransactionsLogUtils.insert(
                            inactiveDailyUser.id,
                            now.toJavaInstant(),
                            TransactionType.INACTIVE_DAILY_TAX,
                            inactiveDailyUser.moneyToBeRemoved,
                            StoredDailyTaxSonhosTransaction(
                                threshold.maxDayThreshold,
                                threshold.minimumSonhosForTrigger,
                                threshold.tax
                            )
                        )

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
                    val bypassDailyTaxUserIds = queryDailyBypassList(m)

                    DailyTaxUtils.getAndProcessInactiveDailyUsers(bypassDailyTaxUserIds, 1) { threshold, inactiveDailyUser ->
                        // Don't warn them about the tax if they were already taxed before
                        if (inactiveDailyUser.id !in alreadyWarnedThatTheyWereTaxed && inactiveDailyUser.id !in alreadyWarnedThatTheyAreGoingToBeTaxed) {
                            DailyTaxWarner.processDailyTaxWarning(
                                threshold,
                                inactiveDailyUser,
                                now,
                                plusXDaysAtMidnight
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