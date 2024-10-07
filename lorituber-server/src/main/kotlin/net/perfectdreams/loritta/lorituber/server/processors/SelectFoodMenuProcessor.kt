package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.SelectFoodMenuRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.SelectFoodMenuResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer

class SelectFoodMenuProcessor(val m: LoriTuberServer) : PacketProcessor<SelectFoodMenuRequest> {
    override suspend fun process(request: SelectFoodMenuRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }

        return SelectFoodMenuResponse(character.data.items)
    }
}