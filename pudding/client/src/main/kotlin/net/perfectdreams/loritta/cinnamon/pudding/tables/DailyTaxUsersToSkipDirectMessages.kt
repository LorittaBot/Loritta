package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object DailyTaxUsersToSkipDirectMessages : LongIdTable() {
    val userId = long("user").index()
    val timestamp = timestamp("timestamp")
}