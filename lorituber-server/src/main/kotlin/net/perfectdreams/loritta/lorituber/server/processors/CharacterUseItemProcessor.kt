package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.bhav.LoriTuberItemBehaviors

class CharacterUseItemProcessor(val m: LoriTuberServer) : PacketProcessor<CharacterUseItemRequest> {
    override suspend fun process(request: CharacterUseItemRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }

        val itemToBeUsed = character.data.items.firstOrNull { it.localId == request.localId }

        if (itemToBeUsed == null)
            error("Unknown Item!")

        val bhav = LoriTuberItemBehaviors.itemToBehaviors[itemToBeUsed.id] ?: error("Attempting to use item (${itemToBeUsed}) but it doesn't have a configured behavior!")

        return bhav.invokeCharacterActionMenu(
            request.itemActionOption,
            m.gameState,
            m.gameState.worldInfo.currentTick,
            character,
            itemToBeUsed
        )
    }
}