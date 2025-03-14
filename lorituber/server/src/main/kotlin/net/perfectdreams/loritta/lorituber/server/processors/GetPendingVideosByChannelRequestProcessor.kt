package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberPendingVideos
import net.perfectdreams.loritta.serializable.lorituber.requests.GetPendingVideosByChannelRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.GetPendingVideosByChannelResponse
import org.jetbrains.exposed.sql.selectAll

class GetPendingVideosByChannelRequestProcessor : LoriTuberRpcProcessor {
    suspend fun process(request: GetPendingVideosByChannelRequest, currentTick: Long, lastUpdate: Long): GetPendingVideosByChannelResponse {
        val pendingVideos = LoriTuberPendingVideos.selectAll().where {
            LoriTuberPendingVideos.channel eq request.channelId
        }.map {
            GetPendingVideosByChannelResponse.PendingVideo(
                it[LoriTuberPendingVideos.contentGenre],
                it[LoriTuberPendingVideos.contentType],
                it[LoriTuberPendingVideos.contentLength],
                it[LoriTuberPendingVideos.scriptScore],
                it[LoriTuberPendingVideos.recordingScore],
                it[LoriTuberPendingVideos.editingScore],
                it[LoriTuberPendingVideos.thumbnailScore],
                it[LoriTuberPendingVideos.percentage],
            )
        }

        return GetPendingVideosByChannelResponse(
            currentTick,
            lastUpdate,
            pendingVideos
        )
    }
}