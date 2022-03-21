package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object DailyTaxUsersToSkipDirectMessages : LongIdTable() {
    val userId = long("user").index()
    val timestamp = timestampWithTimeZone("timestamp")
}