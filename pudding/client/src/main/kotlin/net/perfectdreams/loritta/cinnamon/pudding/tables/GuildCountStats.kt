package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object GuildCountStats : LongIdTable() {
    val timestamp = timestampWithTimeZone("timestamp").index()
    val guildCount = long("guild_count")
}