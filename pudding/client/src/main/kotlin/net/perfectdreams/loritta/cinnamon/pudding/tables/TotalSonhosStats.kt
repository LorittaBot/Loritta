package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object TotalSonhosStats : LongIdTable() {
    val timestamp = timestampWithTimeZone("timestamp").index()
    val totalSonhos = long("total_sonhos")
    val totalSonhosOfBannedUsers = long("total_sonhos_of_banned_users")
    val totalSonhosBroker = long("total_sonhos_broker").nullable()
}