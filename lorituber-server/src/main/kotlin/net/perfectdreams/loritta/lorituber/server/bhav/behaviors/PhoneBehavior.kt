package net.perfectdreams.loritta.lorituber.server.bhav.behaviors

import net.perfectdreams.loritta.lorituber.PhoneCall
import net.perfectdreams.loritta.lorituber.bhav.ObjectActionOption
import net.perfectdreams.loritta.lorituber.bhav.LoriTuberItemBehaviorAttributes
import net.perfectdreams.loritta.lorituber.bhav.UseItemAttributes
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.WorldTime
import net.perfectdreams.loritta.lorituber.server.bhav.CharacterActionInteraction
import net.perfectdreams.loritta.lorituber.server.bhav.CharacterBoundItemBehavior
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.entities.lots.LoriTuberLot

sealed class PhoneBehavior : CharacterBoundItemBehavior<LoriTuberItemBehaviorAttributes.Phone, UseItemAttributes.Phone>() {
    override fun tick(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Phone,
        character: LoriTuberCharacter,
        activeInteraction: CharacterActionInteraction<UseItemAttributes.Phone>?
    ) {
        // TODO: Reimplement this, the issue with the current changes is that there isn't a "owner" for this phone, so we can't get the ticks lived nor the item attributes
        // This is the default behavior "in world" behavior
        val pendingPhoneCallData = behaviorAttributes.pendingPhoneCall

        if (pendingPhoneCallData != null) {
            if (currentTick > pendingPhoneCallData.expiresAt) {
                behaviorAttributes.pendingPhoneCall = null
            }
        } else {
            if (character.data.ticksLived % 2 == 0L) {
                // If we are sleeping, we SHOULD NOT ring their phone
                // Think as if it is in "no disturb" mode :3
                val currentTask = character.data.currentTask
                if (currentTask is LoriTuberTask.UsingItem && currentTask.useItemAttributes is UseItemAttributes.Bed.Sleeping)
                    return

                // Ring their phone!
                val worldTime = WorldTime(currentTick)

                val call = if (true || worldTime.hours in 8..20) {
                    val isPrankCall = gameState.random.nextBoolean()
                    if (isPrankCall) {
                        gameState.oddCalls.random()
                    } else {
                        gameState.sonhosRewardCalls.random()
                    }
                } else {
                    gameState.oddCalls.random()
                }

                behaviorAttributes.pendingPhoneCall = LoriTuberItemBehaviorAttributes.Phone.PendingPhoneCallData(currentTick + 60, call)
            }
        }

        if (activeInteraction != null) {
            when (activeInteraction.useItemAttributes) {
                UseItemAttributes.Phone.DoomscrollingSocialNetwork -> {
                    if (character.motives.isFunFull()) {
                        // Stop current task when it is full
                        character.setTask(null)
                    } else {
                        character.motives.addFunPerTicks(100.0, 240)
                    }
                }
            }
        }
    }


    /* override fun onCharacterItemUse(
        gameState: GameState,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Phone,
        useItemAttributes: UseItemAttributes.Phone
    ): CharacterUseItemResponse {
        when (useItemAttributes) {
            UseItemAttributes.Phone.AnswerCall -> {
                val pendingPhoneCall = behaviorAttributes.pendingPhoneCall
                if (pendingPhoneCall != null) {
                    val phoneCall = pendingPhoneCall.phoneCall
                    behaviorAttributes.pendingPhoneCall = null

                    if (phoneCall is PhoneCall.SonhosReward) {
                        character.addSonhos(phoneCall.sonhosReward)
                    }

                    character.isDirty = true

                    return CharacterUseItemResponse.Success.AnswerCall.Success(pendingPhoneCall.phoneCall)
                } else {
                    return CharacterUseItemResponse.Success.AnswerCall.NoCall
                }
            }
        }
    } */

    fun menuActionDoomscrollSocialNetwork(
        actionOption: ObjectActionOption.DoomscrollSocialNetwork,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Phone
    ): CharacterUseItemResponse {
        character.data.currentTask = LoriTuberTask.UsingItem(
            selfStack.localId,
            UseItemAttributes.Phone.DoomscrollingSocialNetwork
        )
        character.isDirty = true

        return CharacterUseItemResponse.Success.NoAction
    }

    fun menuActionAnswerPhone(
        actionOption: ObjectActionOption.AnswerPhone,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Phone
    ): CharacterUseItemResponse {
        // TODO: Check if we are sleeping and stuffz
        val pendingPhoneCall = behaviorAttributes.pendingPhoneCall
        if (pendingPhoneCall != null) {
            val phoneCall = pendingPhoneCall.phoneCall
            behaviorAttributes.pendingPhoneCall = null

            if (phoneCall is PhoneCall.SonhosReward) {
                character.addSonhos(phoneCall.sonhosReward)
            }

            character.isDirty = true

            return CharacterUseItemResponse.Success.AnswerCall.Success(pendingPhoneCall.phoneCall)
        } else {
            return CharacterUseItemResponse.Success.AnswerCall.NoCall
        }
    }

    override fun actionMenu(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Phone
    ): List<ObjectActionOption> {
        val actions = mutableListOf<ObjectActionOption>()

        // TODO: Check if we are sleeping, we don't receive calls when we are sleeping
        if (behaviorAttributes.pendingPhoneCall != null)
            actions.add(ObjectActionOption.AnswerPhone)

        actions.add(ObjectActionOption.DoomscrollSocialNetwork)
        return actions
    }

    data object BasicPhone : PhoneBehavior()
}