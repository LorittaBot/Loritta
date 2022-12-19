package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberPendingVideos : LongIdTable() {
    val owner = reference("owner", LoriTuberCharacters).index()
    val channel = reference("channel", LoriTuberChannels).index()
    val scriptScore = integer("script_score")
    val recordingScore = integer("recording_score")
    val editingScore = integer("editing_score")
    val thumbnailScore = integer("thumbnail_score")
    val percentage = double("percentage")
}