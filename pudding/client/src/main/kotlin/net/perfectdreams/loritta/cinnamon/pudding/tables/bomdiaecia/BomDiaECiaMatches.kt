package net.perfectdreams.loritta.cinnamon.pudding.tables.bomdiaecia

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object BomDiaECiaMatches : LongIdTable() {
    val startedAt = timestampWithTimeZone("started_at")
    val text = text("text")
}