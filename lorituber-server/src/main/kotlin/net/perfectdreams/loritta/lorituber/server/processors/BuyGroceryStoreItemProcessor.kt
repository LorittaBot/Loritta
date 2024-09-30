package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.BuyGroceryStoreItemRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.BuyGroceryStoreItemResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer

class BuyGroceryStoreItemProcessor(val m: LoriTuberServer) : PacketProcessor<BuyGroceryStoreItemRequest> {
    override suspend fun process(request: BuyGroceryStoreItemRequest): LoriTuberResponse {
        val worldTime = m.gameState.getWorldTime()
        val character = m.gameState.characters.first { it.id == request.characterId }

        // Let's bypass for now
        if (false && worldTime.hours !in 7..18)
            return BuyGroceryStoreItemResponse.Closed

        val groceryItem = m.gameState.nelsonGroceryStore.items.firstOrNull { it.data.item == request.item }
        if (groceryItem == null || 0 >= groceryItem.inStock)
            return BuyGroceryStoreItemResponse.NotInStock

        // Remove the item from the grocery stock
        groceryItem.inStock--

        // Add it to our character's inventory
        character.inventory.addItem(groceryItem.item, 1)

        // Make it dirty!
        // (We don't need to set the character to dirty because it is already set to dirty when adding items)
        m.gameState.nelsonGroceryStore.isDirty = true

        return BuyGroceryStoreItemResponse.Success
    }
}