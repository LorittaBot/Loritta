package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberViewerDislikes : LongIdTable() {
    val owner = reference("owner", LoriTuberViewers)
    val video = reference("video", LoriTuberVideos).index()
    val dislikedAt = timestampWithTimeZone("disliked_at")
    val dislikedAtTicks = long("disliked_ticks")

    val vibe1 = integer("vibe1")
    val vibe2 = integer("vibe2")
    val vibe3 = integer("vibe3")
    val vibe4 = integer("vibe4")
    val vibe5 = integer("vibe5")
    val vibe6 = integer("vibe6")
    val vibe7 = integer("vibe7")
}