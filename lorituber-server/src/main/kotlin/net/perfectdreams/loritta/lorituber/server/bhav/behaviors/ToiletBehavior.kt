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

sealed class ToiletBehavior : LoriTuberItemBehavior<LoriTuberItemBehaviorAttributes.Toilet, UseItemAttributes.Toilet>() {
    private val logger = KotlinLogging.logger {}

    fun menuActionUseToilet(
        actionOption: ItemActionOption.UseToilet,
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Toilet
    ): CharacterUseItemResponse.Success.NoAction {
        character.data.currentTask = LoriTuberTask.UsingItem(
            selfStack.localId,
            UseItemAttributes.Toilet.UsingToilet
        )
        character.isDirty = true

        return CharacterUseItemResponse.Success.NoAction
    }

    fun menuActionUnclogToilet(
        actionOption: ItemActionOption.UnclogToilet,
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Toilet
    ): CharacterUseItemResponse {
        // Well, it isn't clogged, so just ignore!
        if (!behaviorAttributes.isClogged)
            return CharacterUseItemResponse.Success.NoAction

        character.data.currentTask = LoriTuberTask.UsingItem(
            selfStack.localId,
            UseItemAttributes.Toilet.UncloggingToilet
        )

        character.isDirty = true
        return CharacterUseItemResponse.Success.NoAction
    }

    override fun tick(
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Toilet,
        useItemAttributes: UseItemAttributes.Toilet?
    ) {
        when (useItemAttributes) {
            UseItemAttributes.Toilet.UncloggingToilet -> {
                if (!behaviorAttributes.isClogged) {
                    character.setTask(null)
                    return
                }

                behaviorAttributes.unclogTicks--

                if (behaviorAttributes.unclogTicks == 0L) {
                    character.setTask(null)
                    behaviorAttributes.ticksUsedSinceLastUnclog = 0
                    behaviorAttributes.isClogged = false
                }
            }
            UseItemAttributes.Toilet.UsingToilet -> {
                behaviorAttributes.ticksUsedSinceLastUnclog++
                character.motives.addBladderPerTicks(100.0, 15)

                if (character.motives.bladder >= 100.0) {
                    character.setTask(null)

                    // TODO: Move this to a "cancelAction" function
                    behaviorAttributes.isClogged = true
                    behaviorAttributes.unclogTicks = 15
                }
            }
            null -> {}
        }
    }

    override fun actionMenu(
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Toilet
    ): List<ItemActionOption> {
        return if (behaviorAttributes.isClogged) {
            listOf(ItemActionOption.UnclogToilet)
        } else {
            listOf(ItemActionOption.UseToilet)
        }
    }

    data object CheapToilet : ToiletBehavior()
}