package net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object Christmas2022Drops : LongIdTable() {
    val guildId = long("guild")
    val channelId = long("channel")
    val messageId = long("message")
    val createdAt = timestampWithTimeZone("created_at")
}