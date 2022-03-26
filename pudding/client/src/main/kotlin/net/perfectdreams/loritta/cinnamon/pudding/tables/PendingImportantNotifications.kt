package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.exposedpowerutils.sql.jsonb
import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.cinnamon.common.utils.PendingImportantNotificationState
import org.jetbrains.exposed.dao.id.LongIdTable

object PendingImportantNotifications : LongIdTable() {
    val userId = long("user").index()
    val state = postgresEnumeration<PendingImportantNotificationState>("state")
    val submittedAt = timestampWithTimeZone("submitted_at")
    val message = jsonb("message")
}