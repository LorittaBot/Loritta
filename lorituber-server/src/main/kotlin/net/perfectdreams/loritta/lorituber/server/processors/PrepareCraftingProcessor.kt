package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.PrepareCraftingRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.PrepareCraftingResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer

class PrepareCraftingProcessor(val m: LoriTuberServer) : PacketProcessor<PrepareCraftingRequest> {
    override suspend fun process(request: PrepareCraftingRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }

        return PrepareCraftingResponse(character.data.items)
    }
}