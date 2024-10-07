package net.perfectdreams.loritta.lorituber.server.bhav.behaviors

import net.perfectdreams.loritta.lorituber.bhav.ObjectActionOption
import net.perfectdreams.loritta.lorituber.bhav.UseItemAttributes
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.bhav.LotBoundItemBehavior
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.entities.lots.LoriTuberLot

sealed class BedBehavior : LotBoundItemBehavior<Nothing?, UseItemAttributes.Bed>() {
    fun menuActionSleep(
        actionOption: ObjectActionOption.Sleep,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success.NoAction {
        if (isSomeoneUsingThisItemThatIsNotMe(gameState, currentLot, selfStack, character))
            CharacterUseItemResponse.AnotherCharacterIsAlreadyUsingThisItem

        character.data.currentTask = LoriTuberTask.UsingItem(
            selfStack.localId,
            UseItemAttributes.Bed.Sleeping
        )
        character.isDirty = true

        return CharacterUseItemResponse.Success.NoAction
    }

    override fun tick(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?,
        characterInteractions: List<CharacterInteraction<UseItemAttributes.Bed>>
    ) {
        for (activeInteraction in characterInteractions) {
            when (activeInteraction.useItemAttributes) {
                UseItemAttributes.Bed.Sleeping -> {
                    println("Increasing energy motive of " + activeInteraction.character.data.firstName)
                    // 8 hours
                    activeInteraction.character.motives.addEnergyPerTicks(100.0, 480)

                    if (activeInteraction.character.motives.energy >= 100.0)
                        activeInteraction.character.setTask(null)
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
        return listOf(ObjectActionOption.Sleep)
    }

    data object CheapBed : BedBehavior()
}