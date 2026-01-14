package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.common.utils.TrackedChangeType
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object AuditLogEntries : LongIdTable() {
	val guildId = long("guild").index()
	val userId = long("user")
	val ip = text("ip")
	val userAgent = text("user_agent").nullable()
	val changedAt = timestampWithTimeZone("changed_at")
	val trackedChangeType = enumerationByName<TrackedChangeType>("tracked_change_type", 32).index()
}