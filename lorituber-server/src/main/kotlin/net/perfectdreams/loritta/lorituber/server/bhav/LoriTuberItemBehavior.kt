package net.perfectdreams.loritta.lorituber.server.bhav

import net.perfectdreams.loritta.lorituber.bhav.ItemActionOption
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import kotlin.reflect.full.createType
import kotlin.reflect.full.functions

/**
 * Item behaviors that implement custom behaviors for items and stuffz
 */
abstract class LoriTuberItemBehavior<AttrType, UseItemAttrType> {
    fun invokeCharacterActionMenu(
        itemActionOption: ItemActionOption,
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData
    ): CharacterUseItemResponse {
        val behaviorAttributes = selfStack.behaviorAttributes as AttrType
        // if (behaviorAttributes == null)
        //     error("Invalid behavior attributes for item $selfStack! $behaviorAttributes")

        // This is a bit of *magic* but hey, it works and it is better than manually registering all options
        // This also implies that the functions NEED TO BE PUBLIC and CANNOT BE PRIVATE
        val functionToBeCalled = this::class.functions
            .firstOrNull { it.name.startsWith("menuAction") && it.parameters.getOrNull(1)?.type == itemActionOption::class.createType(nullable = false) }
            ?: error("Function for $itemActionOption on $selfStack (bhav: $this) could not be found!")

        return functionToBeCalled.call(this, itemActionOption, gameState, currentTick, character, selfStack, behaviorAttributes) as CharacterUseItemResponse
    }

    fun tick(
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
    ) {
        val behaviorAttributes = selfStack.behaviorAttributes as AttrType
        // if (behaviorAttributes == null)
        //     error("Invalid behavior attributes for item $selfStack! $behaviorAttributes")

        val currentUseItemTask = (character.data.currentTask as? LoriTuberTask.UsingItem)
        if (currentUseItemTask == null || currentUseItemTask.itemLocalId != selfStack.localId) {
            // Not using the current item, tick without the useItemAttributes reference
            tick(
                gameState,
                currentTick,
                character,
                selfStack,
                behaviorAttributes,
                null
            )
            return
        }

        val useItemAttributes = currentUseItemTask.useItemAttributes as? UseItemAttrType
        if (useItemAttributes == null)
            error("Incorrect item attributes for the current item!")

        tick(
            gameState,
            currentTick,
            character,
            selfStack,
            behaviorAttributes,
            useItemAttributes
        )
    }

    abstract fun tick(
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: AttrType,
        useItemAttributes: UseItemAttrType?
    )

    /* fun onCharacterItemUse(
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        useItemAttributes: UseItemAttributes
    ): CharacterUseItemResponse {
        val behaviorAttributes = selfStack.behaviorAttributes as? AttrType
        if (behaviorAttributes == null)
            error("Invalid behavior attributes for item $selfStack! $behaviorAttributes")

        return onCharacterItemUse(
            gameState,
            currentTick,
            character,
            selfStack,
            behaviorAttributes,
            useItemAttributes as UseItemAttrType
        )
    }

    open fun onCharacterItemUse(
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: AttrType,
        useItemAttributes: UseItemAttrType
    ): CharacterUseItemResponse {
        character.data.currentTask = LoriTuberTask.UsingItem(
            selfStack.localId,
            useItemAttributes
        )
        character.isDirty = true

        return CharacterUseItemResponse.Success.NoAction
    } */

    fun actionMenu(
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
    ): List<ItemActionOption> {
        val behaviorAttributes = selfStack.behaviorAttributes as AttrType
        // if (behaviorAttributes == null)
        //     error("Invalid behavior attributes for item $selfStack! $behaviorAttributes")

        return actionMenu(
            gameState,
            currentTick,
            character,
            selfStack,
            behaviorAttributes
        )
    }

    abstract fun actionMenu(
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: AttrType,
    ): List<ItemActionOption>
}