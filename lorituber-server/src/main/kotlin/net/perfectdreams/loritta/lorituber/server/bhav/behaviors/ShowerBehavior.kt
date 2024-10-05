package net.perfectdreams.loritta.lorituber.server.bhav.behaviors

import mu.KotlinLogging
import net.perfectdreams.loritta.lorituber.bhav.ItemActionOption
import net.perfectdreams.loritta.lorituber.bhav.LoriTuberItemBehaviorAttributes
import net.perfectdreams.loritta.lorituber.bhav.UseItemAttributes
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.bhav.LoriTuberItemBehavior
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter

sealed class ShowerBehavior : LoriTuberItemBehavior<LoriTuberItemBehaviorAttributes.Shower, UseItemAttributes.Shower>() {
    private val logger = KotlinLogging.logger {}

    fun menuActionTakeAShower(
        actionOption: ItemActionOption.TakeAShower,
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Shower
    ): CharacterUseItemResponse.Success.NoAction {
        character.data.currentTask = LoriTuberTask.UsingItem(
            selfStack.localId,
            UseItemAttributes.Shower.TakingAShower
        )
        character.isDirty = true

        return CharacterUseItemResponse.Success.NoAction
    }

    override fun tick(
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Shower,
        useItemAttributes: UseItemAttributes.Shower?
    ) {
        when (useItemAttributes) {
            UseItemAttributes.Shower.TakingAShower -> {
                character.motives.addHygienePerTicks(100.0, 20)

                if (character.motives.hygiene >= 100.0)
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
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Shower
    ): List<ItemActionOption> {
        return listOf(ItemActionOption.TakeAShower)
    }

    data object CheapShower : ShowerBehavior()
}