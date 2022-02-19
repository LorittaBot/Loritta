package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.MessageQueuePayload
import net.perfectdreams.loritta.cinnamon.pudding.tables.TaskQueue
import org.jetbrains.exposed.sql.insert
import java.time.Instant

class MessageQueueService(private val pudding: Pudding) : Service(pudding) {
    suspend inline fun <reified T : MessageQueuePayload> appendToMessageQueue(value: T) = appendToMessageQueue(Json.encodeToString<MessageQueuePayload>(value))
    inline fun <reified T : MessageQueuePayload> _appendToMessageQueue(value: T) = _appendToMessageQueue(Json.encodeToString<MessageQueuePayload>(value))

    suspend fun appendToMessageQueue(payload: String) {
        return pudding.transaction {
            TaskQueue.insert {
                it[TaskQueue.queueTime] = Instant.now()
                it[TaskQueue.payload] = payload
            }
        }
    }

    fun _appendToMessageQueue(payload: String) {
        TaskQueue.insert {
            it[TaskQueue.queueTime] = Instant.now()
            it[TaskQueue.payload] = payload
        }
    }
}