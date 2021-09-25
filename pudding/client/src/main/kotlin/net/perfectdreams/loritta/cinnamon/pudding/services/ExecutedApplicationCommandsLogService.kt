package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.json.JsonObject
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.ExecutedApplicationCommandsLog
import org.jetbrains.exposed.sql.insertAndGetId

class ExecutedApplicationCommandsLogService(private val pudding: Pudding) : Service(pudding) {
    suspend fun insertApplicationCommandLog(
        userId: Long,
        guildId: Long?,
        channelId: Long,
        sentAt: Instant,
        declaration: String,
        executor: String,
        options: JsonObject,
        success: Boolean,
        latency: Double,
        stacktrace: String?
    ): Long {
        return pudding.transaction {
            ExecutedApplicationCommandsLog.insertAndGetId {
                it[ExecutedApplicationCommandsLog.userId] = userId
                it[ExecutedApplicationCommandsLog.guildId] = guildId
                it[ExecutedApplicationCommandsLog.channelId] = channelId
                it[ExecutedApplicationCommandsLog.sentAt] = sentAt.toJavaInstant()
                it[ExecutedApplicationCommandsLog.declaration] = declaration
                it[ExecutedApplicationCommandsLog.executor] = executor
                it[ExecutedApplicationCommandsLog.options] = options.toString()
                it[ExecutedApplicationCommandsLog.success] = success
                it[ExecutedApplicationCommandsLog.latency] = latency
                it[ExecutedApplicationCommandsLog.stacktrace] = stacktrace
            }
        }.value
    }
}