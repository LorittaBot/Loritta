package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object TrackedCorreiosPackagesEvents : LongIdTable() {
    val trackingId = text("tracking_id").index()
    val triggeredAt = timestampWithTimeZone("added_at").index()
    val event = jsonb("event")
}