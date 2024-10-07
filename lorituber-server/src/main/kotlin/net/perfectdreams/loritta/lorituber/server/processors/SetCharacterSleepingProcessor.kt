package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.bhav.UseItemAttributes
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.rpc.packets.SetCharacterSleepingRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.SetCharacterSleepingResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer

class SetCharacterSleepingProcessor(val m: LoriTuberServer) : PacketProcessor<SetCharacterSleepingRequest> {
    override suspend fun process(request: SetCharacterSleepingRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }

        if (true) {
            character.data.currentTask = LoriTuberTask.UsingItem(
                character.data.items.first().localId,
                UseItemAttributes.Computer.PlayOnSparklyPower
            )

            character.isDirty = true
            return SetCharacterSleepingResponse
        }
        character.data.currentTask = LoriTuberTask.Sleeping()
        character.isDirty = true

        return SetCharacterSleepingResponse
    }
}