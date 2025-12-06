package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object DropCallsParticipants : LongIdTable() {
    val userId = long("user_id").index()
    val dropCall = reference("drop_call", DropCalls)
    val won = bool("won")
}