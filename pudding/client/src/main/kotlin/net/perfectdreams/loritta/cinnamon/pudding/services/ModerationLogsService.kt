package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationLogs
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.common.utils.ModerationLogAction
import net.perfectdreams.loritta.common.utils.PunishmentAction
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class ModerationLogsService(private val pudding: Pudding) : Service(pudding) {
    companion object {
        fun ResultRow.toPunishmentLog() = PunishmentLog(
            this[ModerationLogs.id].value,
            this[ModerationLogs.guildId],
            this[ModerationLogs.userId],
            this[ModerationLogs.punisherId],
            this[ModerationLogs.punishmentAction],
            this[ModerationLogs.reason],
            this[ModerationLogs.timestamp],
            this[ModerationLogs.expiresAt]
        )
    }

    /**
     * Logs a punishment action to the database
     *
     * @param guildId the guild ID where the punishment was applied
     * @param userId the user ID who was punished
     * @param punisherId the user ID who applied the punishment
     * @param punishmentAction the type of punishment
     * @param reason the reason for the punishment
     * @param expiresAt when the punishment action will expire (only used for MUTE actions)
     */
    suspend fun logPunishment(
        guildId: Long,
        userId: Long,
        punisherId: Long,
        punishmentAction: ModerationLogAction,
        reason: String?,
        expiresAt: Instant?
    ) = pudding.transaction {
        ModerationLogs.insert {
            it[ModerationLogs.guildId] = guildId
            it[ModerationLogs.userId] = userId
            it[ModerationLogs.punisherId] = punisherId
            it[ModerationLogs.punishmentAction] = punishmentAction
            it[ModerationLogs.reason] = reason
            it[ModerationLogs.expiresAt] = expiresAt
            it[timestamp] = Instant.now()
        }
    }

    data class PunishmentLog(
        val id: Long,
        val guildId: Long,
        val userId: Long,
        val punisherId: Long,
        val punishmentAction: ModerationLogAction,
        val reason: String?,
        val timestamp: Instant,
        val muteDuration: Instant?
    )
}
