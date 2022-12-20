package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentGenre
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentLength
import net.perfectdreams.loritta.common.lorituber.LoriTuberContentType
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberPendingVideos : LongIdTable() {
    val owner = reference("owner", LoriTuberCharacters).index()
    val channel = reference("channel", LoriTuberChannels).index()
    val contentGenre = postgresEnumeration<LoriTuberContentGenre>("content_genre")
    val contentType = postgresEnumeration<LoriTuberContentType>("content_type")
    val contentLength = postgresEnumeration<LoriTuberContentLength>("content_length")
    val scriptScore = integer("script_score")
    val recordingScore = integer("recording_score")
    val editingScore = integer("editing_score")
    val thumbnailScore = integer("thumbnail_score")
    val percentage = double("percentage")
}