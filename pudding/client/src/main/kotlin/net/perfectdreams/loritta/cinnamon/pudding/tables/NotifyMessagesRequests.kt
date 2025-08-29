package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object NotifyMessagesRequests : LongIdTable() {
    val userId = long("user").index()
    val channelId = long("channel").index()
    val notifyUserId = long("notify_user").nullable().index()
    val requestedAt = timestampWithTimeZone("requested_at")
    val processedAt = timestampWithTimeZone("processed_at").index().nullable()
}