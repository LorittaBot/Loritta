package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object DropChatParticipants : LongIdTable() {
    val userId = long("user_id").index()
    val dropChat = reference("drop_chat", DropChats)
    val won = bool("won")
}