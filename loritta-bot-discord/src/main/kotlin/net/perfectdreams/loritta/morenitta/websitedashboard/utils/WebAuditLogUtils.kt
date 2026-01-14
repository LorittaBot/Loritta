package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import net.perfectdreams.loritta.cinnamon.pudding.tables.AuditLogEntries
import net.perfectdreams.loritta.common.utils.TrackedChangeType
import net.perfectdreams.loritta.morenitta.utils.Constants
import org.jetbrains.exposed.sql.insert
import java.time.OffsetDateTime

object WebAuditLogUtils {
    const val MAX_ENTRIES_PER_PAGE = 100

    // This is a STUPIDLY SIMPLE audit log entry table
    // In the future it would be better to support more information about each edit, but for now, this'll suffice
    fun addEntry(
        guildId: Long,
        userId: Long,
        ip: String,
        userAgent: String?,
        changeType: TrackedChangeType
    ) {
        AuditLogEntries.insert {
            it[AuditLogEntries.guildId] = guildId
            it[AuditLogEntries.userId] = userId
            it[AuditLogEntries.ip] = ip
            it[AuditLogEntries.userAgent] = userAgent
            it[AuditLogEntries.changedAt] = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)
            it[AuditLogEntries.trackedChangeType] = changeType
        }
    }
}