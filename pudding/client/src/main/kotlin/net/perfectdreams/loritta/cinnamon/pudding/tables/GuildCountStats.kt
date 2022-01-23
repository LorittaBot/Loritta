package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object GuildCountStats : LongIdTable() {
    val timestamp = timestamp("timestamp").index()
    val guildCount = long("guild_count")
}