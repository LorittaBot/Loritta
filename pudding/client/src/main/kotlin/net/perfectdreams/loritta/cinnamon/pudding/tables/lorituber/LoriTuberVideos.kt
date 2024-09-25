package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentCategory
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberVideos : LongIdTable() {
    val owner = reference("owner", LoriTuberCharacters).index()
    val channel = reference("channel", LoriTuberChannels).index()

    val postedAtTicks = long("posted_at_ticks").index()

    val contentCategory = enumerationByName("content_category", 64, LoriTuberVideoContentCategory::class).index()
    val contentLength = postgresEnumeration<LoriTuberContentLength>("content_length")
    val recordingScore = integer("recording_score")
    val editingScore = integer("editing_score")
    val thumbnailScore = integer("thumbnail_score")

    val vibe1 = integer("vibe1")
    val vibe2 = integer("vibe2")
    val vibe3 = integer("vibe3")
    val vibe4 = integer("vibe4")
    val vibe5 = integer("vibe5")
    val vibe6 = integer("vibe6")
    val vibe7 = integer("vibe7")
}