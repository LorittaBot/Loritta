package net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.microservices.dailytax.DailyTax
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DailyTaxTasks(private val m: DailyTax) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val executorService = Executors.newScheduledThreadPool(2)

    private val dailyTaxWarner = DailyTaxWarner(m)
    private val dailyTaxCollector = DailyTaxCollector(m)
    private val pendingMessageProcessor = DailyTaxPendingMessageProcessor(m.config.loritta, m.services, m.rest, m.languageManager)

    fun start() {
        // 12 hours before
        scheduleEveryDayAtSpecificHour(
            LocalTime.of(12, 0),
            dailyTaxWarner
        )

        // 4 hours before
        scheduleEveryDayAtSpecificHour(
            LocalTime.of(20, 0),
            dailyTaxWarner
        )

        // 1 hour before
        scheduleEveryDayAtSpecificHour(
            LocalTime.of(23, 0),
            dailyTaxWarner
        )

        // at midnight + notify about the user about taxes
        scheduleEveryDayAtSpecificHour(
            LocalTime.MIDNIGHT,
            dailyTaxCollector
        )

        // Pending Message Processor
        executorService.scheduleAtFixedRate(pendingMessageProcessor, 0, 1, TimeUnit.SECONDS)

        dailyTaxWarner.run()
    }

    private fun scheduleEveryDayAtSpecificHour(time: LocalTime, runnable: Runnable) {
        val now = Instant.now()
        val today = LocalDate.now(ZoneOffset.UTC)
        val todayAtTime = LocalDateTime.of(today, time)
        val gonnaBeScheduledAtTime =  if (now > todayAtTime.toInstant(ZoneOffset.UTC)) {
            // If today at time is larger than today, then it means that we need to schedule it for tomorrow
            todayAtTime.plusDays(1)
        } else todayAtTime

        val diff = gonnaBeScheduledAtTime.toInstant(ZoneOffset.UTC).toEpochMilli() - System.currentTimeMillis()

        logger.info { "Scheduling ${runnable::class.simpleName} to be executed in ${diff}ms" }
        executorService.scheduleAtFixedRate(
            runnable,
            diff,
            TimeUnit.DAYS.toMillis(1L),
            TimeUnit.MILLISECONDS
        )
    }

    fun shutdown() {
        executorService.shutdown()
    }
}