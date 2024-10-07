package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.bhav.ItemActionRoot
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.ViewCharacterMotivesRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.ViewCharacterMotivesResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.bhav.CharacterBoundItemBehavior
import net.perfectdreams.loritta.lorituber.server.bhav.LoriTuberItemBehaviors
import net.perfectdreams.loritta.lorituber.server.bhav.LotBoundItemBehavior

class ViewCharacterMotivesProcessor(val m: LoriTuberServer) : PacketProcessor<ViewCharacterMotivesRequest> {
    override suspend fun process(request: ViewCharacterMotivesRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }
        val currentLot = m.gameState.lotsById[character.data.currentLotId]!!
        val itemActions = mutableListOf<ItemActionRoot>()

        // Process items that have custom action trees
        for (item in character.data.items) {
            val bhav = LoriTuberItemBehaviors.itemToBehaviors[item.id]

            if (bhav != null && bhav is CharacterBoundItemBehavior<*, *>) {
                // Get which action menus are available to us
                itemActions.add(ItemActionRoot(item, bhav.actionMenu(m.gameState, currentLot, m.gameState.worldInfo.currentTick, character, item)))
            }
        }

        for (item in currentLot.data.items) {
            val bhav = LoriTuberItemBehaviors.itemToBehaviors[item.id]

            if (bhav != null && bhav is LotBoundItemBehavior<*, *>) {
                // Get which action menus are available to us
                itemActions.add(ItemActionRoot(item, bhav.actionMenu(m.gameState, currentLot, m.gameState.worldInfo.currentTick, character, item)))
            }
        }

        // Pass it over to the client
        return ViewCharacterMotivesResponse(
            m.gameState.worldInfo.currentTick,
            character.data.firstName,
            character.data.sonhos,
            character.motives.mood,
            character.data.energyNeed,
            character.data.hungerNeed,
            character.data.funNeed,
            character.data.hygieneNeed,
            character.data.bladderNeed,
            character.data.socialNeed,
            character.data.currentTask,
            currentLot.id,
            character.data.items + currentLot.data.items,
            itemActions
        )
    }
}