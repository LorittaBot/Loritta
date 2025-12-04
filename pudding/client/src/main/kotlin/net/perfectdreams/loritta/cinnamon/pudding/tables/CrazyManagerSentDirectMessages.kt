package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object CrazyManagerSentDirectMessages : LongIdTable() {
    val userId = long("user_id").index()
    val sentAt = timestampWithTimeZone("sent_at")
    val success = bool("success")
    val banEntry = reference("ban_entry", BannedUsers)
}