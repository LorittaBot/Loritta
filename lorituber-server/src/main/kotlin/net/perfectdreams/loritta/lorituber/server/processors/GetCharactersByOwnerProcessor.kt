package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.GetCharactersByOwnerRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GetCharactersByOwnerResponse
import net.perfectdreams.loritta.lorituber.rpc.NetworkLoriTuberCharacter
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer

class GetCharactersByOwnerProcessor(val m: LoriTuberServer) : PacketProcessor<GetCharactersByOwnerRequest> {
    override suspend fun process(request: GetCharactersByOwnerRequest): LoriTuberResponse {
        val characters = m.gameState.characters.filter { it.data.ownerId == request.userId }
            .map {
                NetworkLoriTuberCharacter(
                    it.id,
                    it.data.firstName
                )
            }

        return GetCharactersByOwnerResponse(characters)
    }
}