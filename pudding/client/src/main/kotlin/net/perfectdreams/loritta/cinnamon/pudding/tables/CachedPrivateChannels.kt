package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object CachedPrivateChannels : LongIdTable() {
    val userId = long("user").uniqueIndex()
    val channelId = long("channel")
    val retrievedAt = timestampWithTimeZone("retrieved_at")
    val lastUsedAt = timestampWithTimeZone("last_used")
}