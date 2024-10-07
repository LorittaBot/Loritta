package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.rpc.packets.SetCharacterUsingToiletRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.SetCharacterUsingToiletResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer

class SetCharacterUsingToiletProcessor(val m: LoriTuberServer) : PacketProcessor<SetCharacterUsingToiletRequest> {
    override suspend fun process(request: SetCharacterUsingToiletRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }

        character.data.currentTask = LoriTuberTask.UsingToilet()
        character.isDirty = true

        return SetCharacterUsingToiletResponse
    }
}