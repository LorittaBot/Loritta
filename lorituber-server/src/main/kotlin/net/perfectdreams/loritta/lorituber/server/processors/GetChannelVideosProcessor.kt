package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelVideosRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelVideosResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.NetworkConverter

class GetChannelVideosProcessor(val m: LoriTuberServer) : PacketProcessor<GetChannelVideosRequest> {
    override suspend fun process(request: GetChannelVideosRequest): LoriTuberResponse {
        val videos = m.gameState.videos.filter { it.data.channelId == request.channelId }
        val networkVideos = videos
            .map {
                NetworkConverter.toNetwork(m.gameState, it)
            }

        return GetChannelVideosResponse.Success(networkVideos)
    }
}