package net.perfectdreams.loritta.lorituber.server.bhav.behaviors

import net.perfectdreams.loritta.lorituber.bhav.ObjectActionOption
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.lorituber.server.bhav.LotBoundItemBehavior
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.entities.lots.LoriTuberLot

object ItemStoreBehavior : LotBoundItemBehavior<Nothing?, Nothing?>() {
    fun menuActionBrowseItems(
        actionOption: ObjectActionOption.BrowseStoreItems,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success.ItemStore.BrowseItems {
        val worldTime = gameState.getWorldTime()

        // Let's bypass for now
        if (false && worldTime.hours !in 7..18)
            return CharacterUseItemResponse.Success.ItemStore.BrowseItems.Closed

        return CharacterUseItemResponse.Success.ItemStore.BrowseItems.Success(
            character.data.sonhos,
            gameState.nelsonGroceryStore.items.map { it.data }
        )
    }

    // Not exposed to the user
    fun menuActionBuyItem(
        actionOption: ObjectActionOption.BuyStoreItem,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success {
        val worldTime = gameState.getWorldTime()

        // Let's bypass for now
        if (false && worldTime.hours !in 7..18)
            return CharacterUseItemResponse.Success.ItemStore.BuyItem.Closed

        val groceryItem = gameState.nelsonGroceryStore.items.firstOrNull { it.data.item == actionOption.item }

        if (groceryItem == null || 0 >= groceryItem.inStock)
            return CharacterUseItemResponse.Success.ItemStore.BuyItem.NotInStock

        if (!character.hasSonhos(groceryItem.item.price))
            return CharacterUseItemResponse.Success.ItemStore.BuyItem.NotEnoughSonhos

        // Remove the item from the grocery stock
        groceryItem.inStock--

        // Remove the sonhos
        character.removeSonhos(groceryItem.item.price)

        // Add it to our character's inventory
        character.inventory.addItem(groceryItem.item, 1)

        // Make it dirty!
        // (We don't need to set the character to dirty because it is already set to dirty when adding items)
        gameState.nelsonGroceryStore.isDirty = true

        return CharacterUseItemResponse.Success.ItemStore.BuyItem.Success
    }

    override fun tick(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?,
        characterInteractions: List<CharacterInteraction<Nothing?>>
    ) {
        // TODO: Should we handle item update to here?
    }

    override fun actionMenu(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): List<ObjectActionOption> {
        return listOf(ObjectActionOption.BrowseStoreItems)
    }
}