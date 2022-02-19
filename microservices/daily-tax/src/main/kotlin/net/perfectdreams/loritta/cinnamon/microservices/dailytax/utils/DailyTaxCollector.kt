package net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.dailytax.DailyTax
import net.perfectdreams.loritta.cinnamon.pudding.data.DirectMessageUserDailyTaxTaxedMessageQueuePayload
import net.perfectdreams.loritta.cinnamon.pudding.data.DirectMessageUserDailyTaxWarnMessageQueuePayload
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
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

            m.services.transaction {
                DailyTaxUtils.doSomething(0) { threshold, inactiveDailyUser ->
                    Profiles.update({ Profiles.id eq inactiveDailyUser.id }) {
                        with(SqlExpressionBuilder) {
                            it.update(Profiles.money, Profiles.money - inactiveDailyUser.moneyToBeRemoved)
                        }
                    }

                    val timestampLogId = SonhosTransactionsLog.insertAndGetId {
                        it[SonhosTransactionsLog.user] = inactiveDailyUser.id
                        it[SonhosTransactionsLog.timestamp] = now.toJavaInstant()
                    }

                    // TODO: Transactions
                    /* BrokerSonhosTransactionsLog.insert {
                    it[BrokerSonhosTransactionsLog.timestampLog] = timestampLogId
                    it[BrokerSonhosTransactionsLog.action] = LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction.BOUGHT_SHARES
                    it[BrokerSonhosTransactionsLog.ticker] = tickerId
                    it[BrokerSonhosTransactionsLog.sonhos] = howMuchValue
                    it[BrokerSonhosTransactionsLog.stockPrice] = tickerInformation.value
                    it[BrokerSonhosTransactionsLog.stockQuantity] = quantity
                } */

                    m.services.messageQueue._appendToMessageQueue(
                        DirectMessageUserDailyTaxTaxedMessageQueuePayload(
                            UserId(inactiveDailyUser.id),
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

            logger.info { "Successfully collected today's daily tax!" }
            logger.info { "Checking how many users would be affected if they didn't get the daily between today and tomorrow..." }

            val tomorrowAtMidnight = LocalDateTime.now(ZoneOffset.UTC)
                .plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .toInstant(ZoneOffset.UTC)
                .toKotlinInstant()

            m.services.transaction {
                DailyTaxUtils.doSomething(1) { threshold, inactiveDailyUser ->
                    m.services.messageQueue._appendToMessageQueue(
                        DirectMessageUserDailyTaxWarnMessageQueuePayload(
                            UserId(inactiveDailyUser.id),
                            tomorrowAtMidnight,
                            inactiveDailyUser.money,
                            inactiveDailyUser.moneyToBeRemoved,
                            threshold.maxDayThreshold,
                            threshold.minimumSonhosForTrigger,
                            threshold.tax
                        )
                    )
                }
            }
        } catch (e: Exception) {
            logger.warn { "Something went wrong while collecting tax from inactive daily users!" }
        }
    }
}