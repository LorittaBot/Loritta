package net.perfectdreams.loritta.lorituber.server.bhav.behaviors

import net.perfectdreams.loritta.lorituber.bhav.ItemActionOption
import net.perfectdreams.loritta.lorituber.bhav.UseItemAttributes
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.items.LoriTuberItems
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.bhav.LoriTuberItemBehavior
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter

sealed class FoodBehavior : LoriTuberItemBehavior<Nothing?, UseItemAttributes.Food>() {
    fun menuActionEatFood(
        actionOption: ItemActionOption.EatFood,
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success {
        // Attempt to remove the item from the player inventory
        // TODO: How to handle food eating? If we remove it from the inventory, then we don't have the food anymore
        /* val hadTheItem = character.inventory.removeSingleItem(selfStack.id)

        if (!hadTheItem)
            error("You don't have the item!") */

        character.data.currentTask = LoriTuberTask.UsingItem(
            selfStack.localId,
            UseItemAttributes.Food.EatingFood(gameState.worldInfo.currentTick)
        )
        character.isDirty = true

        return CharacterUseItemResponse.Success.NoAction
    }

    override fun tick(
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?,
        useItemAttributes: UseItemAttributes.Food?
    ) {
        println("Ticking food $useItemAttributes")
        when (useItemAttributes) {
            is UseItemAttributes.Food.EatingFood -> {
                val item = LoriTuberItems.getById(selfStack.id)
                val foodAttributes = item.foodAttributes!!
                if ((currentTick - useItemAttributes.startedEatingAtTick) > foodAttributes.ticks) {
                    // Finished eating the item, remove the task!
                    println("Finished food!")
                    character.setTask(null)
                } else {
                    // We are still eating, nom om om
                    character.motives.addHunger(foodAttributes.hunger.toDouble())
                    character.motives.addBladderPerTicks(-100.0, 120)
                }
            }
            null -> {}
        }
    }

    override fun actionMenu(
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): List<ItemActionOption> {
        return listOf(ItemActionOption.EatFood)
    }

    data object GenericFood : FoodBehavior()
}