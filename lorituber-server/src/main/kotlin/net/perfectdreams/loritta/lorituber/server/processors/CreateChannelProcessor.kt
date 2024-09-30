package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.CreateChannelRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.CreateChannelResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberChannelData
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberChannel

class CreateChannelProcessor(val m: LoriTuberServer) : PacketProcessor<CreateChannelRequest> {
    override suspend fun process(request: CreateChannelRequest): LoriTuberResponse {
        val userAlreadyHasAChannel = m.gameState.channels.any {
            it.id == request.characterId
        }

        if (userAlreadyHasAChannel)
            return CreateChannelResponse.CharacterAlreadyHasTooManyChannels

        val channel = LoriTuberChannel(
            m.gameState.nextChannelId(),
            LoriTuberChannelData(
                request.characterId,
                0,
                request.name,
                mutableListOf(),
                mutableMapOf(),
                mutableMapOf()
            )
        )

        channel.isDirty = true

        m.gameState.channelsById[channel.id] = channel

        return CreateChannelResponse.Success(
            channel.id,
            channel.data.name
        )
    }
}