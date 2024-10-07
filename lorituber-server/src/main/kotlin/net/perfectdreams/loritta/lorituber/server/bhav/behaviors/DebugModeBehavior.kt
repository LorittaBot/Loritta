package net.perfectdreams.loritta.lorituber.server.bhav.behaviors

import net.perfectdreams.loritta.lorituber.bhav.ObjectActionOption
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.lorituber.server.bhav.CharacterActionInteraction
import net.perfectdreams.loritta.lorituber.server.bhav.CharacterBoundItemBehavior
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.entities.lots.LoriTuberLot

object DebugModeBehavior : CharacterBoundItemBehavior<Nothing?, Nothing?>() {
    override fun tick(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?,
        character: LoriTuberCharacter,
        activeInteraction: CharacterActionInteraction<Nothing?>?
    ) {}

    fun menuActionGive1000Sonhos(
        actionOption: ObjectActionOption.Give1000Sonhos,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse {
        character.addSonhos(1_000)
        return CharacterUseItemResponse.Success.NoAction
    }

    fun menuActionViewCurrentLotInfo(
        actionOption: ObjectActionOption.ViewCurrentLotInfo,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse {
        return CharacterUseItemResponse.Success.DebugMode.LotInfo(
            currentLot.id,
            currentLot.data.type
        )
    }

    override fun actionMenu(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): List<ObjectActionOption> {
        return listOf(ObjectActionOption.Give1000Sonhos, ObjectActionOption.ViewCurrentLotInfo)
    }
}