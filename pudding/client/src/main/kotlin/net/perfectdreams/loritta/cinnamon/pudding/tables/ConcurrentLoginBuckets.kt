package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.IntIdTable

object ConcurrentLoginBuckets : IntIdTable() {
    val randomKey = text("random_key")
    val lockedAt = timestampWithTimeZone("locked_at")
}