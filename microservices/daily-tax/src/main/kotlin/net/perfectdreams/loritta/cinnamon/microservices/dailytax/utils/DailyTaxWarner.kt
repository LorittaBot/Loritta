package net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils

import kotlinx.datetime.toKotlinInstant
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.dailytax.DailyTax
import net.perfectdreams.loritta.cinnamon.pudding.data.DirectMessageUserDailyTaxWarnMessageQueuePayload
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import java.time.LocalDateTime
import java.time.ZoneOffset

class DailyTaxWarner(val m: DailyTax) : RunnableCoroutineWrapper() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun runCoroutine() {
        try {
            logger.info { "Checking if someone would lose sonhos to today's daily tax..." }

            val tomorrowAtMidnight = LocalDateTime.now(ZoneOffset.UTC)
                .plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .toInstant(ZoneOffset.UTC)
                .toKotlinInstant()

            m.services.transaction {
                DailyTaxUtils.doSomething(0) { threshold, inactiveDailyUser ->
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

            logger.info { "Successfully warned everyone about today's daily tax!" }
        } catch (e: Exception) {
            logger.warn { "Something went wrong while collecting and sending stats data!" }
        }
    }
}