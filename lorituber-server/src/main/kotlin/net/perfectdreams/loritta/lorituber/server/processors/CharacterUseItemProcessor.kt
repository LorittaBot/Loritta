package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.bhav.CharacterBoundItemBehavior
import net.perfectdreams.loritta.lorituber.server.bhav.LoriTuberItemBehaviors
import net.perfectdreams.loritta.lorituber.server.bhav.LotBoundItemBehavior

class CharacterUseItemProcessor(val m: LoriTuberServer) : PacketProcessor<CharacterUseItemRequest> {
    override suspend fun process(request: CharacterUseItemRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }
        val lot = m.gameState.lotsById[character.data.currentLotId]!!

        // The item to be used may be two different UUIDs:
        // 1. the GLOBAL CHARACTER items
        // 2. the LOT items
        val itemToBeUsed = lot.data.items.firstOrNull { it.localId == request.localId } ?: character.data.items.firstOrNull { it.localId == request.localId }

        if (itemToBeUsed == null)
            error("Item (${character.id}) not present in current lot (${lot.id}) nor in their inventory")

        val bhav = LoriTuberItemBehaviors.itemToBehaviors[itemToBeUsed.id] ?: error("Attempting to use item (${itemToBeUsed}) but it doesn't have a configured behavior!")

        return when (bhav) {
            is CharacterBoundItemBehavior<*, *> -> {
                bhav.invokeCharacterActionMenu(
                    request.objectActionOption,
                    m.gameState,
                    lot,
                    m.gameState.worldInfo.currentTick,
                    character,
                    itemToBeUsed
                )
            }
            is LotBoundItemBehavior<*, *> -> {
                 bhav.invokeCharacterActionMenu(
                    request.objectActionOption,
                    m.gameState,
                    lot,
                    m.gameState.worldInfo.currentTick,
                    character,
                    itemToBeUsed
                )
            }

            else -> error("Unsupported item behavior type $bhav")
        }
    }
}