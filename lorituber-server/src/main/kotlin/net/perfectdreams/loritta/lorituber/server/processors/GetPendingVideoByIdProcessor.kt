package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.GetPendingVideoByIdRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GetPendingVideoByIdResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.NetworkConverter

class GetPendingVideoByIdProcessor(val m: LoriTuberServer) : PacketProcessor<GetPendingVideoByIdRequest> {
    override suspend fun process(request: GetPendingVideoByIdRequest): LoriTuberResponse {
        val channel = m.gameState.channels.firstOrNull { it.id == request.channelId }

        if (channel == null)
            return GetPendingVideoByIdResponse.UnknownChannel

        val pendingVideo = channel.data.pendingVideos.firstOrNull { it.id == request.pendingVideoId }
        if (pendingVideo == null)
            return GetPendingVideoByIdResponse.UnknownPendingVideo

        return GetPendingVideoByIdResponse.Success(NetworkConverter.toNetwork(pendingVideo))
    }
}