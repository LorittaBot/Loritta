package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.LoriTuberVideoStage
import net.perfectdreams.loritta.lorituber.rpc.packets.CreatePendingVideoRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.CreatePendingVideoResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberPendingVideoData

class CreatePendingVideoProcessor(val m: LoriTuberServer) : PacketProcessor<CreatePendingVideoRequest> {
    override suspend fun process(request: CreatePendingVideoRequest): LoriTuberResponse {
        val channel = m.gameState.channels.firstOrNull {
            it.id == request.channelId
        }

        if (channel == null)
            return CreatePendingVideoResponse.UnknownChannel

        if (channel.data.pendingVideos.isNotEmpty())
            return CreatePendingVideoResponse.CharacterIsAlreadyDoingAnotherVideo

        channel.data.pendingVideos.add(
            LoriTuberPendingVideoData(
                channel.nextPendingVideoId(),
                request.contentCategory,
                request.contentLength,
                LoriTuberVideoStage.RECORDING,
                0,
                null,
                null,
                null,
                null
            )
        )

        channel.isDirty = true

        return CreatePendingVideoResponse.Success
    }
}