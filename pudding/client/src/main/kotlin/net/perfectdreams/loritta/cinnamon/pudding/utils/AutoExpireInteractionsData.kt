package net.perfectdreams.loritta.cinnamon.pudding.utils

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.InteractionsData
import org.jetbrains.exposed.sql.deleteWhere
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Automatically expires old interactions data from the database, based on the [InteractionsData.expiresAt] field.
 */
class AutoExpireInteractionsData(private val pudding: Pudding) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val TIME_ZONE = ZoneOffset.UTC
    }

    override fun run() {
        try {
            logger.info { "Removing already expired interactions data..." }

            val now = LocalDate.now(TIME_ZONE)

            runBlocking {
                pudding.transaction {
                    InteractionsData.deleteWhere {
                        InteractionsData.expiresAt less now
                    }
                }
            }

            logger.info { "Successfully expired old interactions data!" }
        } catch (e: Exception) {
            logger.warn(e) { "Somethign went wrong while expiring interactions data!" }
        }
    }
}