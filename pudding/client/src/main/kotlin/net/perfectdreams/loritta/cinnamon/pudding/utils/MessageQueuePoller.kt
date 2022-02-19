package net.perfectdreams.loritta.cinnamon.pudding.utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.MessageQueuePayload
import net.perfectdreams.loritta.cinnamon.pudding.data.UnknownMessageQueuePayload

/**
 * Polls new messages from our message queue.
 */
class MessageQueuePoller(private val pudding: Pudding) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val unknownPayloadIds = mutableSetOf<Long>()

    override fun run() {
        try {
            logger.info { "Polling pending tasks in the queue... Unknown payload IDs: $unknownPayloadIds" }

            val connection = pudding.hikariDataSource.connection
            connection.use {
                val selectStatement = it.prepareStatement("""SELECT id, payload FROM taskqueue WHERE NOT id = ANY(?) ORDER BY id FOR UPDATE SKIP LOCKED LIMIT 10;""")
                selectStatement.setArray(1, connection.createArrayOf("BIGINT", unknownPayloadIds.toTypedArray()))
                val rs = selectStatement.executeQuery()

                var count = 0
                val validIds = mutableSetOf<Long>()
                while (rs.next()) {
                    val id = rs.getLong("id")
                    val payload = try {
                        Json.decodeFromString<MessageQueuePayload>(rs.getString("payload"))
                    } catch (e: Exception) {
                        logger.warn(e) { "Something went wrong while trying to decode payload, this payload will be ignored on future queries and won't be removed from the database if the listener result is false..." }
                        unknownPayloadIds.add(id)
                        UnknownMessageQueuePayload
                    }

                    count++

                    val result = pudding.messageQueueListener?.invoke(payload)
                    logger.info { "Result: $result" }
                    // We will only remove from our task queue if the result is true
                    if (result == true) {
                        validIds.add(id)
                        unknownPayloadIds.remove(id)
                    }
                }

                val deleteStatement = it.prepareStatement("DELETE FROM taskqueue WHERE id = ANY(?);")
                deleteStatement.setArray(1, connection.createArrayOf("BIGINT", validIds.toTypedArray()))
                deleteStatement.execute()

                it.commit()

                logger.info { "Successfully polled $count pending tasks in the queue!" }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while polling pending tasks in the queue!" }
        }
    }
}