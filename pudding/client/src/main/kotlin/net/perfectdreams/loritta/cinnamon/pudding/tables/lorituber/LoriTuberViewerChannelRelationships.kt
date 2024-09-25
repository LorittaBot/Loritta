package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberViewerChannelRelationships : LongIdTable() {
    val owner = reference("owner", LoriTuberViewers).index()
    val channel = reference("channel", LoriTuberChannels).index()
    val relationshipPoints = integer("relationship_points")

    init {
        uniqueIndex(owner, channel)
    }
}