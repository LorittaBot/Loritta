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

object BeachOceanBehavior : LotBoundItemBehavior<Nothing?, UseItemAttributes.BeachOcean>() {
    fun menuActionGoIntoTheSea(
        actionOption: ObjectActionOption.GoIntoTheSea,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success.NoAction {
        character.data.currentTask = LoriTuberTask.UsingItem(
            selfStack.localId,
            UseItemAttributes.BeachOcean.InTheOcean
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
        characterInteractions: List<CharacterInteraction<UseItemAttributes.BeachOcean>>
    ) {
        for (activeInteraction in characterInteractions) {
            when (activeInteraction.useItemAttributes) {
                UseItemAttributes.BeachOcean.InTheOcean -> {
                    if (activeInteraction.character.motives.isFunFull()) {
                        // Stop current task when it is full
                        activeInteraction.character.setTask(null)
                    } else {
                        activeInteraction.character.motives.addFunPerTicks(100.0, 60)
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
        return listOf(ObjectActionOption.GoIntoTheSea)
    }
}