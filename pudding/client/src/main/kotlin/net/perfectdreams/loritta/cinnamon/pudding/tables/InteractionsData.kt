package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object InteractionsData : LongIdTable() {
    val data = jsonb("data")
    val addedAt = timestampWithTimeZone("added_at")
    val expiresAt = timestampWithTimeZone("expires_at")
}