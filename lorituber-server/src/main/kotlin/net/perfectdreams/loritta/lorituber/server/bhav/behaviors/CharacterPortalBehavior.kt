package net.perfectdreams.loritta.lorituber.server.bhav.behaviors

import net.perfectdreams.loritta.lorituber.SpecialLots
import net.perfectdreams.loritta.lorituber.bhav.ObjectActionOption
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.lorituber.server.bhav.LotBoundItemBehavior
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.entities.lots.LoriTuberLot
import java.util.*

object CharacterPortalBehavior : LotBoundItemBehavior<Nothing?, Nothing?>() {
    private data class PortalAction(
        val actionOption: ObjectActionOption,
        val lotId: (LoriTuberCharacter) -> (UUID),
    )

    private val allPortalActions = listOf(
        PortalAction(ObjectActionOption.GoBackToHome) { it.data.mainLotId },
        PortalAction(ObjectActionOption.GoOutside) { SpecialLots.OUTSIDE },
        PortalAction(ObjectActionOption.GoToNelsonGroceryStore) { SpecialLots.NELSON_GROCERY_STORE },
        PortalAction(ObjectActionOption.GoToStarryBeach) { SpecialLots.STARRY_BEACH },
    )

    fun menuActionGoOutside(
        actionOption: ObjectActionOption.GoOutside,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success.NoAction {
        teleportCharacter(gameState, character, SpecialLots.OUTSIDE)
        return CharacterUseItemResponse.Success.NoAction
    }

    fun menuActionGoBackToHome(
        actionOption: ObjectActionOption.GoBackToHome,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success.NoAction {
        teleportCharacter(gameState, character, character.data.mainLotId)
        return CharacterUseItemResponse.Success.NoAction
    }

    fun menuActionGoToStarryBeach(
        actionOption: ObjectActionOption.GoToStarryBeach,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success.NoAction {
        teleportCharacter(gameState, character, SpecialLots.STARRY_BEACH)
        return CharacterUseItemResponse.Success.NoAction
    }

    fun menuActionGoToNelsonGroceryStore(
        actionOption: ObjectActionOption.GoToNelsonGroceryStore,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): CharacterUseItemResponse.Success.NoAction {
        teleportCharacter(gameState, character, SpecialLots.NELSON_GROCERY_STORE)
        return CharacterUseItemResponse.Success.NoAction
    }

    private fun teleportCharacter(gameState: GameState, character: LoriTuberCharacter, destinationId: UUID) {
        val lot = gameState.lotsById[destinationId] ?: error("Invalid destination lot!")
        lot.teleportCharacterToHere(character)
    }

    override fun tick(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?,
        characterInteractions: List<CharacterInteraction<Nothing?>>
    ) {}

    override fun actionMenu(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: Nothing?
    ): List<ObjectActionOption> {
        // The actions change depending on what is the current lot
        return allPortalActions.filter {
            it.lotId.invoke(character) != currentLot.id
        }.map { it.actionOption }
    }
}