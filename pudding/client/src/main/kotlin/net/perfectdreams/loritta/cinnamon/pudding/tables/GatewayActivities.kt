package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.IntIdTable

object GatewayActivities : IntIdTable() {
    val text = text("text")
    val type = text("type")
    val streamUrl = text("stream_url").nullable()
    val priority = integer("priority")
    val submittedAt = timestampWithTimeZone("submitted_at")
    val startsAt = timestampWithTimeZone("starts_at").index()
    val endsAt = timestampWithTimeZone("ends_at").index()
}