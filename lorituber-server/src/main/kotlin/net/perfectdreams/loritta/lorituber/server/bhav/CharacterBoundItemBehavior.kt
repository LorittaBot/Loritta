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
 * An Item behavior that is globally bound to a character
 */
abstract class CharacterBoundItemBehavior<AttrType, UseItemAttrType> : ItemBehavior() {
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

        // WE DO NOT CHECK FOR "ANOTHER USER IS ALREADY USING THIS ITEM" FOR GLOBAL ITEMS!!!

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
        character: LoriTuberCharacter
    ) {
        val behaviorAttributes = selfStack.behaviorAttributes as AttrType
        // if (behaviorAttributes == null)
        //     error("Invalid behavior attributes for item $selfStack! $behaviorAttributes")

        var activeInteraction: CharacterActionInteraction<UseItemAttrType>? = null

        val currentTask = character.data.currentTask
        if (currentTask != null && currentTask is LoriTuberTask.UsingItem) {
            // Are we using this item?
            if (currentTask.itemLocalId == selfStack.localId) {
                // Yes, we are!
                val useItemAttributes = currentTask.useItemAttributes as? UseItemAttrType
                if (useItemAttributes == null)
                    error("Incorrect item attributes for the current item!")

                activeInteraction = CharacterActionInteraction(
                    character,
                    currentTask,
                    useItemAttributes
                )
            }
        }

        tick(
            gameState,
            currentLot,
            currentTick,
            selfStack,
            behaviorAttributes,
            character,
            activeInteraction
        )
    }

    abstract fun tick(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: AttrType,
        character: LoriTuberCharacter,
        activeInteraction: CharacterActionInteraction<UseItemAttrType>?
    )

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

    abstract fun actionMenu(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: AttrType,
    ): List<ObjectActionOption>
}