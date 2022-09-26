package net.perfectdreams.loritta.cinnamon.pudding.tables.notifications

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import org.jetbrains.exposed.dao.id.LongIdTable

object UserNotifications : LongIdTable() {
    val timestamp = timestampWithTimeZone("timestamp")
    val user = reference("user", Profiles).index()
}