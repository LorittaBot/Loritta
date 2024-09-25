package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoResolution
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoStage
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberPendingVideos : LongIdTable() {
    val owner = reference("owner", LoriTuberCharacters).index()
    val channel = reference("channel", LoriTuberChannels).index()
    val contentCategory = postgresEnumeration<LoriTuberVideoContentCategory>("content_category")
    val contentLength = postgresEnumeration<LoriTuberContentLength>("content_length")

    // The current development stage of the video
    val currentStage = enumerationByName("current_stage", 64, LoriTuberVideoStage::class)

    // The current stage progress in ticks
    val currentStageProgressTicks = long("current_stage_progress_ticks")
    val recordingScore = integer("recording_score").nullable()
    val editingScore = integer("editing_score").nullable()
    val thumbnailScore = integer("thumbnail_score").nullable()
    val videoResolution = enumerationByName("video_resolution", 64, LoriTuberVideoResolution::class).nullable()

    // The vibes
    val vibe1 = integer("vibe1")
    val vibe2 = integer("vibe2")
    val vibe3 = integer("vibe3")
    val vibe4 = integer("vibe4")
    val vibe5 = integer("vibe5")
    val vibe6 = integer("vibe6")
    val vibe7 = integer("vibe7")
}