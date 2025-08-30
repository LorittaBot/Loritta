package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.common.utils.NotificationType
import org.jetbrains.exposed.dao.id.LongIdTable

object UserNotificationSettings : LongIdTable() {
    val userId = long("user").index()
    val type = enumerationByName<NotificationType>("type", 64).index()
    val enabled = bool("enabled").index()
    val configuredAt = timestampWithTimeZone("configured_at")

    init {
        uniqueIndex(userId, type)
    }
}