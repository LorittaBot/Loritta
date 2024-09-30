package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.SetCharacterMotivesRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.SetCharacterMotivesResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer

class SetCharacterMotivesProcessor(val m: LoriTuberServer) : PacketProcessor<SetCharacterMotivesRequest> {
    override suspend fun process(request: SetCharacterMotivesRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }

        character.data.energyNeed = request.energyNeed
        character.data.energyNeed = request.energyNeed
        character.data.socialNeed = request.socialNeed
        character.data.funNeed = request.funNeed
        character.data.bladderNeed = request.bladderNeed
        character.data.hygieneNeed = request.hygieneNeed
        character.data.hungerNeed = request.hungerNeed

        character.isDirty = true

        return SetCharacterMotivesResponse
    }
}