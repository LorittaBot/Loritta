package net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.dailytax.DailyTax
import net.perfectdreams.loritta.cinnamon.pudding.data.UserDailyTaxTaxedDirectMessage
import net.perfectdreams.loritta.cinnamon.pudding.data.UserDailyTaxWarnDirectMessage
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyTaxSonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import java.sql.Connection
import java.time.LocalDateTime
import java.time.ZoneOffset

class DailyTaxCollector(val m: DailyTax) : RunnableCoroutineWrapper() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun runCoroutine() {
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

            // We need to use Read Commited to avoid "Could not serialize access due to concurrent update"
            // This is more "unsafe" because we may make someone be in the negative sonhos, but there isn't another good alterative, so yeah...
            m.services.transaction(transactionIsolation = Connection.TRANSACTION_READ_COMMITTED) {
                DailyTaxUtils.getAndProcessInactiveDailyUsers(m.config.discord.applicationId, 0) { threshold, inactiveDailyUser ->
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

                    m.services.users._insertPendingDailyTaxDirectMessage(
                        UserId(inactiveDailyUser.id),
                        UserDailyTaxTaxedDirectMessage(
                            now,
                            nextTrigger,
                            inactiveDailyUser.money,
                            inactiveDailyUser.moneyToBeRemoved,
                            threshold.maxDayThreshold,
                            threshold.minimumSonhosForTrigger,
                            threshold.tax
                        )
                    )
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

                m.services.transaction {
                    DailyTaxUtils.getAndProcessInactiveDailyUsers(m.config.discord.applicationId, 1) { threshold, inactiveDailyUser ->
                        // Don't warn them about the tax if they were already taxed before
                        if (!alreadyWarnedThatTheyWereTaxed.contains(inactiveDailyUser.id)) {
                            m.services.users._insertPendingDailyTaxDirectMessage(
                                UserId(inactiveDailyUser.id),
                                UserDailyTaxWarnDirectMessage(
                                    plusXDaysAtMidnight,
                                    now,
                                    inactiveDailyUser.money,
                                    inactiveDailyUser.moneyToBeRemoved,
                                    threshold.maxDayThreshold,
                                    threshold.minimumSonhosForTrigger,
                                    threshold.tax
                                )
                            )
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