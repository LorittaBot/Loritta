package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object Sponsors : LongIdTable() {
    val name = text("name")
    val startsAt = timestampWithTimeZone("starts_at").index()
    val endsAt = timestampWithTimeZone("ends_at").nullable().index()
    val banners = jsonb("banners")
    val slug = text("slug").index()
    val link = text("link")
    val enabled = bool("enabled")
    val sponsorValue = double("sponsor_value").index()
}