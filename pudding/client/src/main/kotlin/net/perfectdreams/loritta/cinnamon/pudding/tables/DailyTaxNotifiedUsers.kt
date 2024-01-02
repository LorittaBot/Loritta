package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object DailyTaxNotifiedUsers : LongIdTable() {
    val user = reference("user", Profiles).uniqueIndex()
    val notifiedAt = timestampWithTimeZone("notified_at")
}