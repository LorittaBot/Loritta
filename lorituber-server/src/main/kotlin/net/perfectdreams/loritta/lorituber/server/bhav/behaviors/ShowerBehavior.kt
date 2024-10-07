package net.perfectdreams.loritta.lorituber.server.bhav.behaviors

import mu.KotlinLogging
import net.perfectdreams.loritta.lorituber.bhav.ObjectActionOption
import net.perfectdreams.loritta.lorituber.bhav.LoriTuberItemBehaviorAttributes
import net.perfectdreams.loritta.lorituber.bhav.UseItemAttributes
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.bhav.LotBoundItemBehavior
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.entities.lots.LoriTuberLot

sealed class ShowerBehavior : LotBoundItemBehavior<LoriTuberItemBehaviorAttributes.Shower, UseItemAttributes.Shower>() {
    private val logger = KotlinLogging.logger {}

    fun menuActionTakeAShower(
        actionOption: ObjectActionOption.TakeAShower,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Shower
    ): CharacterUseItemResponse.Success.NoAction {
        if (isSomeoneUsingThisItemThatIsNotMe(gameState, currentLot, selfStack, character))
            CharacterUseItemResponse.AnotherCharacterIsAlreadyUsingThisItem

        character.data.currentTask = LoriTuberTask.UsingItem(
            selfStack.localId,
            UseItemAttributes.Shower.TakingAShower
        )
        character.isDirty = true

        return CharacterUseItemResponse.Success.NoAction
    }

    override fun tick(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Shower,
        characterInteractions: List<CharacterInteraction<UseItemAttributes.Shower>>
    ) {
        for (activeInteraction in characterInteractions) {
            val useItemAttributes = activeInteraction.useItemAttributes
            when (useItemAttributes) {
                UseItemAttributes.Shower.TakingAShower -> {
                    activeInteraction.character.motives.addHygienePerTicks(100.0, 20)

                    if (activeInteraction.character.motives.hygiene >= 100.0)
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
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Shower
    ): List<ObjectActionOption> {
        return listOf(ObjectActionOption.TakeAShower)
    }

    data object CheapShower : ShowerBehavior()
}