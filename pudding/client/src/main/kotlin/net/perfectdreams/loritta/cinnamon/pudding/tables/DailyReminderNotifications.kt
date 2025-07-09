package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object DailyReminderNotifications : LongIdTable() {
    val userId = long("user").index()
    val submittedAt = timestampWithTimeZone("submitted_at").index()
    val triggeredForDaily = timestampWithTimeZone("triggered_for_daily")
    val processedAt = timestampWithTimeZone("processed_at").index().nullable()
    val successfullySent = bool("successfully_sent")
}