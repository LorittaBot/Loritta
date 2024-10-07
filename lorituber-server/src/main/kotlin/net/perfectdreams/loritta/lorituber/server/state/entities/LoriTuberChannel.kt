package net.perfectdreams.loritta.lorituber.server.state.entities

import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberChannelData
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberSuperViewerChannelRelationshipData
import java.util.*

data class LoriTuberChannel(
    val id: UUID,
    val data: LoriTuberChannelData
) : LoriTuberEntity() {
    fun nextPendingVideoId(): Long {
        this.isDirty = true
        return data.pendingVideoCounter++
    }

    /* fun addRelationshipOfSuperViewer(superViewer: LoriTuberSuperViewer, score: Int) = addRelationshipOfSuperViewer(superViewer.id, score)

    fun addRelationshipOfSuperViewer(superViewerId: Long, score: Int) {
        val relData = data.channelRelationships.getOrPut(superViewerId) {
            LoriTuberSuperViewerChannelRelationshipData(
                0,
                0
            )
        }
        val newValue = (relData.relationshipScore + score).coerceIn(0..100)

        // If there wasn't a change, then we don't need to mark it as dirty
        if (newValue != relData.relationshipScore) {
            relData.relationshipScore = newValue
            isDirty = true
        }
    } */

    fun addRelationshipOfCategory(category: LoriTuberVideoContentCategory, score: Int) {
        val relData = data.channelRelationshipsV2.getOrPut(category) {
            LoriTuberSuperViewerChannelRelationshipData(
                0,
                0
            )
        }
        val newValue = (relData.relationshipScore + score).coerceIn(0..100)

        // If there wasn't a change, then we don't need to mark it as dirty
        if (newValue != relData.relationshipScore) {
            relData.relationshipScore = newValue
            isDirty = true
        }
    }

    fun addCategoryLevel(category: LoriTuberVideoContentCategory, levelsToIncrease: Int) {
        data.categoryLevels[category] = (data.categoryLevels.getOrDefault(category, 1) + levelsToIncrease).coerceIn(1..100)
        isDirty = true
    }
}