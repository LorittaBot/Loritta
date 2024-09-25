package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberPendingVideos
import net.perfectdreams.loritta.serializable.lorituber.requests.CreatePendingVideoRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.CreatePendingVideoResponse
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

class CreatePendingVideoRequestProcessor : LoriTuberRpcProcessor {
    suspend fun process(request: CreatePendingVideoRequest, currentTick: Long, lastUpdate: Long): CreatePendingVideoResponse {
        if (LoriTuberPendingVideos.select { LoriTuberPendingVideos.owner eq request.characterId }.count() != 0L) {
            return CreatePendingVideoResponse.CharacterIsAlreadyDoingAnotherVideo(
                currentTick,
                lastUpdate
            )
        }

        val pendingVideoId = LoriTuberPendingVideos.insertAndGetId {
            it[LoriTuberPendingVideos.owner] = request.characterId
            it[LoriTuberPendingVideos.channel] = request.channelId
            it[LoriTuberPendingVideos.contentLength] = request.contentLength
            it[LoriTuberPendingVideos.contentGenre] = request.contentGenre
            it[LoriTuberPendingVideos.contentType] = request.contentType
            it[LoriTuberPendingVideos.scriptScore] = 0
            it[LoriTuberPendingVideos.recordingScore] = 0
            it[LoriTuberPendingVideos.editingScore] = 0
            it[LoriTuberPendingVideos.thumbnailScore] = 0
            it[LoriTuberPendingVideos.renderingProgress] = 0.0
        }

        return CreatePendingVideoResponse.Success(
            currentTick,
            lastUpdate,
            pendingVideoId.value
        )
    }
}