package net.perfectdreams.loritta.lorituber.server.bhav.behaviors

import net.perfectdreams.loritta.lorituber.bhav.ObjectActionOption
import net.perfectdreams.loritta.lorituber.bhav.UseItemAttributes
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.items.LoriTuberItems
import net.perfectdreams.loritta.lorituber.recipes.LoriTuberRecipes
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.bhav.LotBoundItemBehavior
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.entities.lots.LoriTuberLot
import net.perfectdreams.loritta.lorituber.server.state.items.toItem

sealed class FridgeBehavior : LotBoundItemBehavior<Nothing?, UseItemAttributes.Fridge>() {
    fun menuActionPrepareFoodMenu(
        actionOption: ObjectActionOption.PrepareFoodMenu,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success {
        return CharacterUseItemResponse.Success.Fridge.PrepareFoodMenu(character.data.items)
    }

    fun menuActionPrepareFood(
        actionOption: ObjectActionOption.PrepareFood,
        gameState: GameState,
        currentLot: LoriTuberLot,
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
        actionOption: ObjectActionOption.EatFoodMenu,
        gameState: GameState,
        currentLot: LoriTuberLot,
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
        currentLot: LoriTuberLot,
        currentTick: Long,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?,
        characterInteractions: List<CharacterInteraction<UseItemAttributes.Fridge>>
    ) {
        for (activeInteraction in characterInteractions) {
            when (activeInteraction.useItemAttributes) {
                is UseItemAttributes.Fridge.PreparingFood -> {
                    val recipe = activeInteraction.useItemAttributes.recipeId?.let { LoriTuberRecipes.getById(it) }
                    val targetItem = recipe?.targetItemId?.let { LoriTuberItems.getById(it) } ?: LoriTuberItems.SLOP

                    val ticks = recipe?.ticks ?: 20 // Slop

                    if ((currentTick - activeInteraction.useItemAttributes.startedPreparingAtTick) > ticks) {
                        // Finished eating the item, remove the task!
                        println("Finished preparing food!")

                        // Remove the items from the inventory if the user has them
                        val itemsFromTheUserInventory =
                            activeInteraction.character.data.items.filter { it.id in activeInteraction.useItemAttributes.items }

                        val hasEnoughItems =
                            activeInteraction.character.inventory.containsItems(activeInteraction.useItemAttributes.items)

                        if (!hasEnoughItems) {
                            // We don't have enough items! Just reset the task and bail out!
                            activeInteraction.character.setTask(null)
                        } else {
                            for (item in itemsFromTheUserInventory) {
                                activeInteraction.character.inventory.removeSingleItem(item.id)
                            }

                            // Add the new item to their inventory
                            activeInteraction.character.inventory.addItem(targetItem, 1)

                            // Update the task to null
                            activeInteraction.character.setTask(null)
                        }
                    } else {
                        // We are still preparing, do nothing
                    }
                }
            }
        }
    }

    override fun actionMenu(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): List<ObjectActionOption> {
        return listOf(ObjectActionOption.EatFoodMenu, ObjectActionOption.PrepareFoodMenu)
    }

    data object CheapFridge : FridgeBehavior()
}