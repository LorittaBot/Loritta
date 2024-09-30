package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberChannel
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelByIdRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelByIdResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.NetworkConverter

class GetChannelByIdProcessor(val m: LoriTuberServer) : PacketProcessor<GetChannelByIdRequest> {
    override suspend fun process(request: GetChannelByIdRequest): LoriTuberResponse {
        val channel = m.gameState.channels.firstOrNull { it.id == request.channelId }

        if (channel == null)
            return GetChannelByIdResponse.UnknownChannel

        return GetChannelByIdResponse.Success(
            NetworkLoriTuberChannel(
                channel.id,
                channel.data.name,
                channel.data.pendingVideos.map {
                    NetworkConverter.toNetwork(it)
                }
            )
        )
    }
}