package net.perfectdreams.loritta.lorituber.server.bhav.behaviors

import net.perfectdreams.loritta.lorituber.bhav.ItemActionOption
import net.perfectdreams.loritta.lorituber.bhav.UseItemAttributes
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.items.LoriTuberItems
import net.perfectdreams.loritta.lorituber.recipes.LoriTuberRecipes
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.bhav.LoriTuberItemBehavior
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.items.toItem

sealed class FridgeBehavior : LoriTuberItemBehavior<Nothing?, UseItemAttributes.Fridge>() {
    fun menuActionPrepareFoodMenu(
        actionOption: ItemActionOption.PrepareFoodMenu,
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success {
        return CharacterUseItemResponse.Success.Fridge.PrepareFoodMenu(character.data.items)
    }

    fun menuActionPrepareFood(
        actionOption: ItemActionOption.PrepareFood,
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success {
        val matchedRecipe = LoriTuberRecipes.getMatchingRecipeForItems(actionOption.items)

        character.data.currentTask = LoriTuberTask.UsingItem(
            selfStack.localId,
            UseItemAttributes.Fridge.PreparingFood(
                matchedRecipe?.id,
                actionOption.items,
                gameState.worldInfo.currentTick
            )
        )
        character.isDirty = true

        return CharacterUseItemResponse.Success.NoAction
    }

    // Technically this ain't the food themselves
    fun menuActionEatFoodMenu(
        actionOption: ItemActionOption.EatFoodMenu,
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success {
        return CharacterUseItemResponse.Success.Fridge.EatFoodMenu(
            character.data.items.filter {
                it.id.toItem().foodAttributes != null
            }
        )
    }

    /* fun menuActionEatFood(
        actionOption: ItemActionOption.EatFood,
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success.NoAction {
        // TODO: This requires a special "action" to open a menu
        return CharacterUseItemResponse.Success.NoAction
    } */

    override fun tick(
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?,
        useItemAttributes: UseItemAttributes.Fridge?
    ) {
        when (useItemAttributes) {
            is UseItemAttributes.Fridge.PreparingFood -> {
                val recipe = useItemAttributes.recipeId?.let { LoriTuberRecipes.getById(it) }
                val targetItem = recipe?.targetItemId?.let { LoriTuberItems.getById(it) } ?: LoriTuberItems.SLOP

                val ticks = recipe?.ticks ?: 20 // Slop

                if ((currentTick - useItemAttributes.startedPreparingAtTick) > ticks) {
                    // Finished eating the item, remove the task!
                    println("Finished preparing food!")

                    // Remove the items from the inventory if the user has them
                    val itemsFromTheUserInventory = character.data.items.filter { it.id in useItemAttributes.items }

                    val hasEnoughItems = character.inventory.containsItems(useItemAttributes.items)

                    if (!hasEnoughItems) {
                        // We don't have enough items! Just reset the task and bail out!
                        character.setTask(null)
                    } else {
                        for (item in itemsFromTheUserInventory) {
                            character.inventory.removeSingleItem(item.id)
                        }

                        // Add the new item to their inventory
                        character.inventory.addItem(targetItem, 1)

                        // Update the task to null
                        character.setTask(null)
                    }
                } else {
                    // We are still preparing, do nothing
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
        return listOf(ItemActionOption.EatFoodMenu, ItemActionOption.PrepareFoodMenu)
    }

    data object CheapFridge : FridgeBehavior()
}