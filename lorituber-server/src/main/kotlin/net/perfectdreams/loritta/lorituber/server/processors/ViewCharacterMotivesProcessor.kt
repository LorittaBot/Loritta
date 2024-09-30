package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.ViewCharacterMotivesRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.ViewCharacterMotivesResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer

class ViewCharacterMotivesProcessor(val m: LoriTuberServer) : PacketProcessor<ViewCharacterMotivesRequest> {
    override suspend fun process(request: ViewCharacterMotivesRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }

        return ViewCharacterMotivesResponse(
            m.gameState.worldInfo.currentTick,
            character.data.firstName,
            character.data.energyNeed,
            character.data.hungerNeed,
            character.data.funNeed,
            character.data.hygieneNeed,
            character.data.bladderNeed,
            character.data.socialNeed,
            character.data.currentTask
        )
    }
}