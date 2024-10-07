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

sealed class ToiletBehavior : LotBoundItemBehavior<LoriTuberItemBehaviorAttributes.Toilet, UseItemAttributes.Toilet>() {
    private val logger = KotlinLogging.logger {}

    fun menuActionUseToilet(
        actionOption: ObjectActionOption.UseToilet,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Toilet
    ): CharacterUseItemResponse.Success.NoAction {
        if (isSomeoneUsingThisItemThatIsNotMe(gameState, currentLot, selfStack, character))
            CharacterUseItemResponse.AnotherCharacterIsAlreadyUsingThisItem

        character.data.currentTask = LoriTuberTask.UsingItem(
            selfStack.localId,
            UseItemAttributes.Toilet.UsingToilet
        )
        character.isDirty = true

        return CharacterUseItemResponse.Success.NoAction
    }

    fun menuActionUnclogToilet(
        actionOption: ObjectActionOption.UnclogToilet,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Toilet
    ): CharacterUseItemResponse {
        // Well, it isn't clogged, so just ignore!
        if (!behaviorAttributes.isClogged)
            return CharacterUseItemResponse.Success.NoAction

        if (isSomeoneUsingThisItemThatIsNotMe(gameState, currentLot, selfStack, character))
            CharacterUseItemResponse.AnotherCharacterIsAlreadyUsingThisItem

        character.data.currentTask = LoriTuberTask.UsingItem(
            selfStack.localId,
            UseItemAttributes.Toilet.UncloggingToilet
        )

        character.isDirty = true
        return CharacterUseItemResponse.Success.NoAction
    }

    override fun tick(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Toilet,
        characterInteractions: List<CharacterInteraction<UseItemAttributes.Toilet>>
    ) {
        for (activeInteraction in characterInteractions) {
            val useItemAttributes = activeInteraction.useItemAttributes

            when (useItemAttributes) {
                UseItemAttributes.Toilet.UncloggingToilet -> {
                    if (!behaviorAttributes.isClogged) {
                        activeInteraction.character.setTask(null)
                        return
                    }

                    behaviorAttributes.unclogTicks--

                    if (behaviorAttributes.unclogTicks == 0L) {
                        activeInteraction.character.setTask(null)
                        behaviorAttributes.ticksUsedSinceLastUnclog = 0
                        behaviorAttributes.isClogged = false
                    }
                }

                UseItemAttributes.Toilet.UsingToilet -> {
                    behaviorAttributes.ticksUsedSinceLastUnclog++
                    activeInteraction.character.motives.addBladderPerTicks(100.0, 15)

                    if (activeInteraction.character.motives.bladder >= 100.0) {
                        activeInteraction.character.setTask(null)

                        // TODO: Move this to a "cancelAction" function
                        behaviorAttributes.isClogged = true
                        behaviorAttributes.unclogTicks = 15
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
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Toilet
    ): List<ObjectActionOption> {
        return if (behaviorAttributes.isClogged) {
            listOf(ObjectActionOption.UnclogToilet)
        } else {
            listOf(ObjectActionOption.UseToilet)
        }
    }

    data object CheapToilet : ToiletBehavior()
}