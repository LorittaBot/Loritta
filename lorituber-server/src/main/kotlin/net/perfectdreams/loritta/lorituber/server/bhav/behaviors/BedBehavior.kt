package net.perfectdreams.loritta.lorituber.server.bhav.behaviors

import net.perfectdreams.loritta.lorituber.bhav.ItemActionOption
import net.perfectdreams.loritta.lorituber.bhav.UseItemAttributes
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.bhav.LoriTuberItemBehavior
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter

sealed class BedBehavior : LoriTuberItemBehavior<Nothing?, UseItemAttributes.Bed>() {
    fun menuActionSleep(
        actionOption: ItemActionOption.Sleep,
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success.NoAction {
        character.data.currentTask = LoriTuberTask.UsingItem(
            selfStack.localId,
            UseItemAttributes.Bed.Sleeping
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
        useItemAttributes: UseItemAttributes.Bed?
    ) {
        when (useItemAttributes) {
            UseItemAttributes.Bed.Sleeping -> {
                // 8 hours
                character.motives.addEnergyPerTicks(100.0, 480)

                if (character.motives.energy >= 100.0)
                    character.setTask(null)
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
        return listOf(ItemActionOption.Sleep)
    }

    data object CheapBed : BedBehavior()
}