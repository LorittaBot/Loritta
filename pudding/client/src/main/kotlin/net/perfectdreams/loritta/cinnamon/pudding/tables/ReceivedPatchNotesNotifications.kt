package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object ReceivedPatchNotesNotifications : LongIdTable() {
    val patchNotesNotification = reference("patch_notes_notification", PatchNotesNotifications)
    val user = reference("user", Profiles)
    val receivedAt = timestampWithTimeZone("received_at")
}