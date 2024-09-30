package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberChannel
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelsByCharacterRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GetChannelsByCharacterResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.NetworkConverter

class GetChannelsByCharacterProcessor(val m: LoriTuberServer) : PacketProcessor<GetChannelsByCharacterRequest> {
    override suspend fun process(request: GetChannelsByCharacterRequest): LoriTuberResponse {
        val characters = m.gameState.channels.filter { it.data.characterId == request.characterId }
            .map {
                NetworkLoriTuberChannel(
                    it.id,
                    it.data.name,
                    it.data.pendingVideos.map {
                        NetworkConverter.toNetwork(it)
                    }
                )
            }

        return GetChannelsByCharacterResponse(characters)
    }
}