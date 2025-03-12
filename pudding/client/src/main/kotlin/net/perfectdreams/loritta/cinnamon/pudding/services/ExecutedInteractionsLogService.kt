package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.json.JsonObject
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.ExecutedApplicationCommandsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.ExecutedComponentsLog
import net.perfectdreams.loritta.common.commands.ApplicationCommandType
import net.perfectdreams.loritta.common.commands.InteractionContextType
import net.perfectdreams.loritta.common.components.ComponentType
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

class ExecutedInteractionsLogService(private val pudding: Pudding) : Service(pudding) {
    suspend fun insertApplicationCommandLog(
        userId: Long,
        guildId: Long?,
        channelId: Long,
        sentAt: Instant,
        type: ApplicationCommandType,
        declaration: String,
        executor: String,
        options: JsonObject,
        success: Boolean,
        latency: Double,
        stacktrace: String?,
        context: InteractionContextType,
        guildIntegrationId: Long?,
        userIntegrationId: Long?,
    ): Long {
        return pudding.transaction {
            ExecutedApplicationCommandsLog.insertAndGetId {
                it[ExecutedApplicationCommandsLog.userId] = userId
                it[ExecutedApplicationCommandsLog.guildId] = guildId
                it[ExecutedApplicationCommandsLog.channelId] = channelId
                it[ExecutedApplicationCommandsLog.sentAt] = sentAt.toJavaInstant()
                it[ExecutedApplicationCommandsLog.type] = type
                it[ExecutedApplicationCommandsLog.declaration] = declaration
                it[ExecutedApplicationCommandsLog.executor] = executor
                it[ExecutedApplicationCommandsLog.options] = options.toString()
                it[ExecutedApplicationCommandsLog.success] = success
                it[ExecutedApplicationCommandsLog.latency] = latency
                it[ExecutedApplicationCommandsLog.context] = context
                it[ExecutedApplicationCommandsLog.guildIntegration] = guildIntegrationId
                it[ExecutedApplicationCommandsLog.userIntegration] = userIntegrationId
                it[ExecutedApplicationCommandsLog.stacktrace] = stacktrace
            }
        }.value
    }

    suspend fun insertComponentLog(
        userId: Long,
        guildId: Long?,
        channelId: Long,
        sentAt: Instant,
        type: ComponentType,
        declaration: String,
        executor: String,
        // options: JsonObject,
        success: Boolean,
        latency: Double,
        stacktrace: String?
    ): Long {
        return pudding.transaction {
            ExecutedComponentsLog.insertAndGetId {
                it[ExecutedComponentsLog.userId] = userId
                it[ExecutedComponentsLog.guildId] = guildId
                it[ExecutedComponentsLog.channelId] = channelId
                it[ExecutedComponentsLog.sentAt] = sentAt.toJavaInstant()
                it[ExecutedComponentsLog.type] = type
                it[ExecutedComponentsLog.declaration] = declaration
                it[ExecutedComponentsLog.executor] = executor
                // it[ExecutedApplicationCommandsLog.options] = options.toString()
                it[ExecutedComponentsLog.success] = success
                it[ExecutedComponentsLog.latency] = latency
                it[ExecutedComponentsLog.stacktrace] = stacktrace
            }
        }.value
    }

    suspend fun getExecutedApplicationCommands(since: Instant): Long {
        return pudding.transaction {
            return@transaction ExecutedApplicationCommandsLog.selectAll().where {
                ExecutedApplicationCommandsLog.sentAt greaterEq since.toJavaInstant()
            }.count()
        }
    }

    suspend fun getUniqueUsersExecutedApplicationCommands(since: Instant): Long {
        return pudding.transaction {
            return@transaction ExecutedApplicationCommandsLog.select(ExecutedApplicationCommandsLog.userId).where { 
                ExecutedApplicationCommandsLog.sentAt greaterEq since.toJavaInstant()
            }.groupBy(ExecutedApplicationCommandsLog.userId).count()
        }
    }
}