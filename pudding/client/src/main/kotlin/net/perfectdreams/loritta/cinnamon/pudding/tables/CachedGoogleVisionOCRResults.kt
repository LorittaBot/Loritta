package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object CachedGoogleVisionOCRResults : LongIdTable() {
    val url = text("url").index()
    val receivedAt = timestampWithTimeZone("received_at")
    val result = jsonb("result")
}