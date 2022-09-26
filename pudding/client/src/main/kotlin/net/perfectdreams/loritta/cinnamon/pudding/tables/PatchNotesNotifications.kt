package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object PatchNotesNotifications : LongIdTable() {
    val submittedAt = timestampWithTimeZone("submitted_at")
    val broadcastAfter = timestampWithTimeZone("broadcast_after").index()
    val expiresAt = timestampWithTimeZone("expires_at").index()
    val path = text("path")
}