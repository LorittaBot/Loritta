package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.rpc.packets.SetCharacterTakingAShowerRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.SetCharacterTakingAShowerResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer

class SetCharacterTakingAShowerProcessor(val m: LoriTuberServer) : PacketProcessor<SetCharacterTakingAShowerRequest> {
    override suspend fun process(request: SetCharacterTakingAShowerRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }

        character.data.currentTask = LoriTuberTask.TakingAShower()
        character.isDirty = true

        return SetCharacterTakingAShowerResponse
    }
}