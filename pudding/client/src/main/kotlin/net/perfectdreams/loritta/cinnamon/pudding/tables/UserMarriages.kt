package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object UserMarriages : LongIdTable() {
    val createdAt = timestampWithTimeZone("created_at").index()
    val active = bool("active").index()
    val expiredAt = timestampWithTimeZone("expired_at").nullable().index()
    val affinity = integer("affinity").index()
    val coupleName = text("couple_name").nullable()
    val coupleBadge = uuid("couple_badge").nullable()
}