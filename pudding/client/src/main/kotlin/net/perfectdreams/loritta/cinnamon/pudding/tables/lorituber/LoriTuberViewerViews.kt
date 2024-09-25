package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberViewerViews : LongIdTable() {
    val owner = reference("owner", LoriTuberViewers).index()
    val video = reference("video", LoriTuberVideos).index()
    val viewedAt = timestampWithTimeZone("viewed_at")
    val viewedAtTicks = long("viewed_at_ticks")
}