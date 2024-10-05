package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.bhav.ItemActionRoot
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.ViewCharacterMotivesRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.ViewCharacterMotivesResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.bhav.LoriTuberItemBehaviors

class ViewCharacterMotivesProcessor(val m: LoriTuberServer) : PacketProcessor<ViewCharacterMotivesRequest> {
    override suspend fun process(request: ViewCharacterMotivesRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }

        val itemActions = mutableListOf<ItemActionRoot>()
        // Process items that have custom action trees
        for (item in character.data.items) {
            val bhav = LoriTuberItemBehaviors.itemToBehaviors[item.id]

            if (bhav != null) {
                // Get which action menus are available to us
                itemActions.add(ItemActionRoot(item, bhav.actionMenu(m.gameState, m.gameState.worldInfo.currentTick, character, item)))
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
            character.data.items,
            itemActions
        )
    }
}