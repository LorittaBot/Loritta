package net.perfectdreams.loritta.lorituber.server.bhav

import net.perfectdreams.loritta.lorituber.bhav.ObjectActionOption
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.entities.lots.LoriTuberLot
import kotlin.reflect.full.createType
import kotlin.reflect.full.functions

/**
 * Item behaviors that implement custom behaviors for items and stuffz
 */
abstract class LotBoundItemBehavior<AttrType, UseItemAttrType> : ItemBehavior() {
    data class CharacterInteractionBuilder(
        val character: LoriTuberCharacter,
        val useItemTask: LoriTuberTask.UsingItem
    )

    data class CharacterInteraction<UseItemAttrType>(
        val character: LoriTuberCharacter,
        val useItemTask: LoriTuberTask.UsingItem,
        val useItemAttributes: UseItemAttrType
    )

    fun invokeCharacterActionMenu(
        objectActionOption: ObjectActionOption,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData
    ): CharacterUseItemResponse {
        val behaviorAttributes = selfStack.behaviorAttributes as AttrType
        // if (behaviorAttributes == null)
        //     error("Invalid behavior attributes for item $selfStack! $behaviorAttributes")

        // Multi-user checks are done within the menuAction themselves, because some actions may allow multiple users to use the item

        // TODO: Should we validate if the menu is still available? (Rerun the menuAction and check it?)

        // This is a bit of *magic* but hey, it works and it is better than manually registering all options
        // This also implies that the functions NEED TO BE PUBLIC and CANNOT BE PRIVATE
        val functionToBeCalled = this::class.functions
            .firstOrNull { it.name.startsWith("menuAction") && it.parameters.getOrNull(1)?.type == objectActionOption::class.createType(nullable = false) }
            ?: error("Function for $objectActionOption on $selfStack (bhav: $this) could not be found!")

        return functionToBeCalled.call(this, objectActionOption, gameState, currentLot, currentTick, character, selfStack, behaviorAttributes) as CharacterUseItemResponse
    }

    fun tick(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        selfStack: LoriTuberItemStackData,
        characterInteractions: List<CharacterInteractionBuilder>
    ) {
        val behaviorAttributes = selfStack.behaviorAttributes as AttrType
        // if (behaviorAttributes == null)
        //     error("Invalid behavior attributes for item $selfStack! $behaviorAttributes")

        val trueCharacterInteractions = characterInteractions.map {
            val useItemAttributes = it.useItemTask.useItemAttributes as? UseItemAttrType
            if (useItemAttributes == null)
                error("Incorrect item attributes for the current item!")

            CharacterInteraction<UseItemAttrType>(
                it.character,
                it.useItemTask,
                useItemAttributes
            )
        }

        tick(
            gameState,
            currentLot,
            currentTick,
            selfStack,
            behaviorAttributes,
            trueCharacterInteractions
        )
    }

    abstract fun tick(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: AttrType,
        characterInteractions: List<CharacterInteraction<UseItemAttrType>>
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
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
    ): List<ObjectActionOption> {
        val behaviorAttributes = selfStack.behaviorAttributes as AttrType
        // if (behaviorAttributes == null)
        //     error("Invalid behavior attributes for item $selfStack! $behaviorAttributes")

        return actionMenu(
            gameState,
            currentLot,
            currentTick,
            character,
            selfStack,
            behaviorAttributes
        )
    }

    /**
     * Checks if another character is already using this item
     */
    fun isSomeoneUsingThisItemThatIsNotMe(
        gameState: GameState,
        currentLot: LoriTuberLot,
        selfStack: LoriTuberItemStackData,
        character: LoriTuberCharacter
    ): Boolean {
        for (otherCharacter in gameState.characters) {
            if (character == otherCharacter)
                continue // It is ourselves, next!

            if (otherCharacter.data.currentLotId != currentLot.id)
                continue // The character is not in the current lot! Next!

            val usingItemTask = (otherCharacter.data.currentTask as? LoriTuberTask.UsingItem)
            if (usingItemTask?.itemLocalId != selfStack.localId)
                continue // They aren't using this item! Next!

            // Yup, someone else is using this item!
            return true
        }

        // No one is using this item!
        return false
    }

    fun isSomeoneUsingThisItem(gameState: GameState, currentLot: LoriTuberLot, selfStack: LoriTuberItemStackData): Boolean {
        return gameState.characters.any {
            it.data.currentLotId == currentLot.id && (it.data.currentTask as? LoriTuberTask.UsingItem)?.itemLocalId == selfStack.localId
        }
    }

    private fun getCharactersThatAreUsingThisItem(gameState: GameState, currentLot: LoriTuberLot, selfStack: LoriTuberItemStackData) {
        gameState.characters.filter {
            it.data.currentLotId == currentLot.id && (it.data.currentTask as? LoriTuberTask.UsingItem)?.itemLocalId == selfStack.localId
        }
    }

    abstract fun actionMenu(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: AttrType,
    ): List<ObjectActionOption>
}