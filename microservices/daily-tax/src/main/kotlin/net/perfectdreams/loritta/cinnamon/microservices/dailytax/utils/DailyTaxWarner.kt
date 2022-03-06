package net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.dailytax.DailyTax
import net.perfectdreams.loritta.cinnamon.pudding.data.UserDailyTaxWarnDirectMessage
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.DailyTaxPendingDirectMessages
import org.jetbrains.exposed.sql.deleteAll
import java.time.LocalDateTime
import java.time.ZoneOffset

class DailyTaxWarner(val m: DailyTax) : RunnableCoroutineWrapper() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun runCoroutine() {
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

            m.services.transaction {
                // Delete all pending direct messages because we will replace with newer messages
                DailyTaxPendingDirectMessages.deleteAll()

                DailyTaxUtils.getAndProcessInactiveDailyUsers(m.config.discord.applicationId, 0) { threshold, inactiveDailyUser ->
                    m.services.users._insertPendingDailyTaxDirectMessage(
                        UserId(inactiveDailyUser.id),
                        UserDailyTaxWarnDirectMessage(
                            tomorrowAtMidnight,
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

            logger.info { "Successfully warned everyone about today's daily tax!" }
        } catch (e: Exception) {
            logger.warn { "Something went wrong while collecting and sending stats data!" }
        }
    }
}