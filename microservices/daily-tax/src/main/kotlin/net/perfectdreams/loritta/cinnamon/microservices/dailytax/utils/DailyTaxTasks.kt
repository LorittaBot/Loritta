package net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils

import net.perfectdreams.loritta.cinnamon.microservices.dailytax.DailyTax
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DailyTaxTasks(private val m: DailyTax) {
    private val executorService = Executors.newScheduledThreadPool(1)

    fun start() {
        run {
            val midnightLessOneHour = LocalTime.MIDNIGHT.minusHours(1)
            val today = LocalDate.now(ZoneOffset.UTC)
            val todayMidnight = LocalDateTime.of(today, midnightLessOneHour)
            val tomorrowMidnight = todayMidnight.plusDays(1)
            val diff = tomorrowMidnight.toInstant(ZoneOffset.UTC).toEpochMilli() - System.currentTimeMillis()

            executorService.scheduleAtFixedRate(
                DailyTaxWarner(m),
                diff,
                TimeUnit.DAYS.toMillis(1L),
                TimeUnit.MILLISECONDS
            )
        }

        run {
            val midnight = LocalTime.MIDNIGHT
            val today = LocalDate.now(ZoneOffset.UTC)
            val todayMidnight = LocalDateTime.of(today, midnight)
            val tomorrowMidnight = todayMidnight.plusDays(1)
            val diff = tomorrowMidnight.toInstant(ZoneOffset.UTC).toEpochMilli() - System.currentTimeMillis()

            executorService.scheduleAtFixedRate(
                DailyTaxCollector(m),
                diff,
                TimeUnit.DAYS.toMillis(1L),
                TimeUnit.MILLISECONDS
            )
        }
    }

    fun shutdown() {
        executorService.shutdown()
    }
}