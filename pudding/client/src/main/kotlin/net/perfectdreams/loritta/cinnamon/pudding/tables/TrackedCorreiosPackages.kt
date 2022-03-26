package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object TrackedCorreiosPackages : LongIdTable() {
    val trackingId = text("tracking_id").index()
    val addedAt = timestampWithTimeZone("added_at")
    val delivered = bool("delivered").index()
    val unknownPackage = bool("unknown_package").index()
}