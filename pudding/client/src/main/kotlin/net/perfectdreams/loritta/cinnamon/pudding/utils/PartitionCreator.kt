package net.perfectdreams.loritta.cinnamon.pudding.utils

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Automatically creates partitions for tables.
 *
 * Table partitions are useful for tables that have a lot of data, but you only need to retrieve recent data from them (example: logs).
 */
class PartitionCreator(private val pudding: Pudding) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val TIME_ZONE = ZoneOffset.UTC
    }

    override fun run() {
        try {
            logger.info { "Creating Partition Tables..." }

            // Create current month partition
            runBlocking {
                val now = LocalDate.now(TIME_ZONE)

                // Create this month's partition
                createTablePartition(now)

                // Create next month's partition
                createTablePartition(now.plusMonths(1L))
            }

            logger.info { "Successfully created partition tables!" }
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while creating partitions!" }
        }
    }

    private suspend fun createTablePartition(now: LocalDate) {
        val beginningOfTheMonth = now
            .withDayOfMonth(1)

        // The partition is EXCLUSIVE, not INCLUSIVE!
        val endOfTheMonth = now
            .withDayOfMonth(1)
            .plusMonths(1L)
            .withDayOfMonth(1)

        val beginningYear = beginningOfTheMonth.year
        val beginningMonth = beginningOfTheMonth.monthValue.toString().padStart(2, '0')
        val beginningDay = beginningOfTheMonth.dayOfMonth.toString().padStart(2, '0')

        val endYear = endOfTheMonth.year
        val endMonth = endOfTheMonth.monthValue.toString().padStart(2, '0')
        val endDay = endOfTheMonth.dayOfMonth.toString().padStart(2, '0')

        pudding.transaction {
            exec("CREATE TABLE IF NOT EXISTS executedapplicationcommandslog_y${beginningYear}m$beginningMonth PARTITION OF executedapplicationcommandslog FOR VALUES FROM ('$beginningYear-$beginningMonth-$beginningDay') TO ('$endYear-$endMonth-$endDay');")
            exec("CREATE TABLE IF NOT EXISTS executedcomponentslog_y${beginningYear}m$beginningMonth PARTITION OF executedcomponentslog FOR VALUES FROM ('$beginningYear-$beginningMonth-$beginningDay') TO ('$endYear-$endMonth-$endDay');")
        }
    }
}