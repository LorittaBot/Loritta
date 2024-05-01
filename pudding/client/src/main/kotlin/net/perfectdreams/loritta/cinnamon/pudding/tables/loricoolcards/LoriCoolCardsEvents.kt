package net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriCoolCardsEvents : LongIdTable() {
    val eventName = text("event_name")
    val startsAt = timestampWithTimeZone("starts_at").index()
    val endsAt = timestampWithTimeZone("ends_at").index()
    val template = jsonb("template")
}