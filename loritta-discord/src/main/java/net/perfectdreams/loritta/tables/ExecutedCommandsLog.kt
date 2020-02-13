package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object ExecutedCommandsLog : LongIdTable() {
    val userId = long("user").index()
    val guildId = long("guild").nullable().index()
    val channelId = long("channel").index()
    val sentAt = long("sent_at")
    val command = text("command").index()
    val message = text("message")
}