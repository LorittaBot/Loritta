package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.items.LoriTuberItems
import net.perfectdreams.loritta.lorituber.rpc.packets.EatFoodRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.EatFoodResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer

class EatFoodProcessor(val m: LoriTuberServer) : PacketProcessor<EatFoodRequest> {
    override suspend fun process(request: EatFoodRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }

        // Is this even edible?
        val item = LoriTuberItems.getById(request.itemId)
        if (item.foodAttributes == null)
            return EatFoodResponse.ItemNotEdible // nuh uh, it isn't!

        // Attempt to remove the item from the player inventory
        val hadTheItem = character.inventory.removeSingleItem(request.itemId)

        if (!hadTheItem)
            return EatFoodResponse.ItemNotFound // Whoops, you don't actually have the item!

        // Update the character task
        character.setTask(LoriTuberTask.Eating(item.id, m.gameState.worldInfo.currentTick))

        return EatFoodResponse.Success
    }
}