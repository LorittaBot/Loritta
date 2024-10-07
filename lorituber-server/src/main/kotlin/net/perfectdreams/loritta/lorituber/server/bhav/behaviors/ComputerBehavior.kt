package net.perfectdreams.loritta.lorituber.server.bhav.behaviors

import mu.KotlinLogging
import net.perfectdreams.loritta.lorituber.LoriTuberVideoStage
import net.perfectdreams.loritta.lorituber.bhav.ObjectActionOption
import net.perfectdreams.loritta.lorituber.bhav.LoriTuberItemBehaviorAttributes
import net.perfectdreams.loritta.lorituber.bhav.UseItemAttributes
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.rpc.packets.CharacterUseItemResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.bhav.LotBoundItemBehavior
import net.perfectdreams.loritta.lorituber.server.state.GameState
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberPendingVideoStageData
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.entities.lots.LoriTuberLot

sealed class ComputerBehavior : LotBoundItemBehavior<LoriTuberItemBehaviorAttributes.Computer, UseItemAttributes.Computer>() {
    private val logger = KotlinLogging.logger {}

    abstract val parallelTasks: Int

    fun menuActionPlayOnSparklyPower(
        actionOption: ObjectActionOption.PlayOnSparklyPower,
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        character: LoriTuberCharacter,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Computer
    ): CharacterUseItemResponse {
        if (isSomeoneUsingThisItemThatIsNotMe(gameState, currentLot, selfStack, character))
            CharacterUseItemResponse.AnotherCharacterIsAlreadyUsingThisItem

        // TODO: Are we rendering something? If yes, then we can't use the computer until the user stops it
        // TODO: Check the parallel tasks
        if (behaviorAttributes.videoRenderTask != null) {
            error("Can't do new task because we are rendering a video")
        }
        character.data.currentTask = LoriTuberTask.UsingItem(
            selfStack.localId,
            UseItemAttributes.Computer.PlayOnSparklyPower
        )
        character.isDirty = true

        return CharacterUseItemResponse.Success.NoAction
    }

    override fun tick(
        gameState: GameState,
        currentLot: LoriTuberLot,
        currentTick: Long,
        selfStack: LoriTuberItemStackData,
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Computer,
        characterInteractions: List<CharacterInteraction<UseItemAttributes.Computer>>
    ) {
        logger.info { "I'm ticking! Current ticks lived: ${behaviorAttributes.ticksLived}" }

        behaviorAttributes.ticksLived++

        // A character is using the current item
        if (characterInteractions.isNotEmpty()) {
            for (activeInteraction in characterInteractions) {
                logger.info { "I'm actually using the damn item!" }

                val renderingVideo = behaviorAttributes.videoRenderTask
                if (renderingVideo != null && parallelTasks == 1) {
                    // Cancel the current render task
                    behaviorAttributes.videoRenderTask = null
                }

                when (activeInteraction.useItemAttributes) {
                    UseItemAttributes.Computer.PlayOnSparklyPower -> {
                        if (activeInteraction.character.motives.isFunFull()) {
                            // Stop current task when it is full
                            activeInteraction.character.setTask(null)
                        } else {
                            activeInteraction.character.motives.addFunPerTicks(100.0, 60)
                        }
                    }

                    is UseItemAttributes.Computer.WorkOnVideo -> {
                        if (!activeInteraction.character.motives.isMoodAboveRequiredForWork()) {
                            // I'm too depressed, reset the task!
                            activeInteraction.character.setTask(null)
                            return
                        }

                        val channel = gameState.channelsById[activeInteraction.useItemAttributes.channelId]
                        if (channel == null) {
                            // Unknown channel, reset the task!
                            logger.warn { "Channel ${activeInteraction.useItemAttributes.channelId} does not exist! Bailing out..." }
                            activeInteraction.character.setTask(null)
                            return
                        }

                        val pendingVideo =
                            channel.data.pendingVideos.firstOrNull { it.id == activeInteraction.useItemAttributes.pendingVideoId }
                        if (pendingVideo == null) {
                            // Unknown pending video, reset the task!
                            logger.warn { "Pending Video ${activeInteraction.useItemAttributes.pendingVideoId} does not exist! Bailing out..." }
                            activeInteraction.character.setTask(null)
                            return
                        }

                        when (activeInteraction.useItemAttributes.stage) {
                            LoriTuberVideoStage.RECORDING -> {
                                val inProgress = pendingVideo.recordingStage as? LoriTuberPendingVideoStageData.InProgress
                                if (inProgress == null) {
                                    // Invalid stage, reset!
                                    activeInteraction.character.setTask(null)
                                } else {
                                    // How this works?
                                    // We +1 for each tick
                                    if (inProgress.progressTicks == 5L) {
                                        // Okay, so this stage is finished! Cancel the current task
                                        activeInteraction.character.setTask(null)

                                        // Set the new recording score...
                                        pendingVideo.recordingStage = LoriTuberPendingVideoStageData.Finished(
                                            gameState.random.nextInt(
                                                10,
                                                21
                                            )
                                        )

                                        // And unlock two new stages!
                                        pendingVideo.editingStage = LoriTuberPendingVideoStageData.InProgress(0)
                                        pendingVideo.thumbnailStage = LoriTuberPendingVideoStageData.InProgress(0)
                                    } else {
                                        inProgress.progressTicks++
                                    }
                                    channel.isDirty = true
                                }
                            }

                            LoriTuberVideoStage.EDITING -> {
                                val inProgress = pendingVideo.editingStage as? LoriTuberPendingVideoStageData.InProgress
                                if (inProgress == null) {
                                    // Invalid stage, reset!
                                    activeInteraction.character.setTask(null)
                                } else {
                                    // How this works?
                                    // We +1 for each tick
                                    if (inProgress.progressTicks == 5L) {
                                        // Okay, so this stage is finished! Cancel the current task
                                        activeInteraction.character.setTask(null)

                                        // Set the new recording score...
                                        pendingVideo.editingStage = LoriTuberPendingVideoStageData.Finished(
                                            gameState.random.nextInt(
                                                10,
                                                21
                                            )
                                        )

                                        // And unlock the next stage!
                                        pendingVideo.renderingStage = LoriTuberPendingVideoStageData.InProgress(0)
                                    } else {
                                        inProgress.progressTicks++
                                    }
                                    channel.isDirty = true
                                }
                            }

                            LoriTuberVideoStage.RENDERING -> {
                                val inProgress = pendingVideo.renderingStage as? LoriTuberPendingVideoStageData.InProgress
                                if (inProgress == null) {
                                    // Invalid stage, reset!
                                    activeInteraction.character.setTask(null)
                                } else {
                                    // How this works?
                                    // We +1 for each tick
                                    if (inProgress.progressTicks == 5L) {
                                        // Okay, so this stage is finished! Cancel the current task
                                        activeInteraction.character.setTask(null)

                                        // Set the new recording score...
                                        pendingVideo.renderingStage = LoriTuberPendingVideoStageData.Finished(
                                            gameState.random.nextInt(
                                                10,
                                                21
                                            )
                                        )

                                        // And now we don't need to unlock anything rn
                                    } else {
                                        inProgress.progressTicks++
                                    }
                                    channel.isDirty = true
                                }
                            }

                            LoriTuberVideoStage.THUMBNAIL -> {
                                val inProgress = pendingVideo.thumbnailStage as? LoriTuberPendingVideoStageData.InProgress
                                if (inProgress == null) {
                                    // Invalid stage, reset!
                                    activeInteraction.character.setTask(null)
                                } else {
                                    // How this works?
                                    // We +1 for each tick
                                    if (inProgress.progressTicks == 5L) {
                                        // Okay, so this stage is finished! Cancel the current task
                                        activeInteraction.character.setTask(null)

                                        // Set the new recording score...
                                        pendingVideo.thumbnailStage = LoriTuberPendingVideoStageData.Finished(
                                            gameState.random.nextInt(
                                                10,
                                                21
                                            )
                                        )

                                        // And now we don't need to unlock anything rn
                                    } else {
                                        inProgress.progressTicks++
                                    }
                                    channel.isDirty = true
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val videoRenderTask = behaviorAttributes.videoRenderTask
            if (videoRenderTask != null) {
                val channel = gameState.channelsById[videoRenderTask.channelId]
                if (channel == null) {
                    // Unknown channel, delete the task!
                    behaviorAttributes.videoRenderTask = null
                    return
                }

                val pendingVideo = channel.data.pendingVideos.firstOrNull { it.id == videoRenderTask.pendingVideoId }
                if (pendingVideo == null) {
                    // Unknown pending video, delete the task!
                    behaviorAttributes.videoRenderTask = null
                    return
                }

                // We are rendering a video!
                when (val stage = pendingVideo.renderingStage) {
                    is LoriTuberPendingVideoStageData.InProgress -> {
                        if (stage.progressTicks == 1000L) {
                            // Finished rendering, let's party!
                            behaviorAttributes.videoRenderTask = null

                            pendingVideo.renderingStage = LoriTuberPendingVideoStageData.Finished(0) // The score for the rendering does not matter
                            channel.isDirty = true
                        }

                        stage.progressTicks++
                    }
                    is LoriTuberPendingVideoStageData.Finished -> {
                        error("Attempting to render a pending video (${videoRenderTask.pendingVideoId}) of channel (${videoRenderTask.channelId}) but the stage is finished!")
                    }
                    LoriTuberPendingVideoStageData.Unavailable -> {
                        error("Attempting to render a pending video (${videoRenderTask.pendingVideoId}) of channel (${videoRenderTask.channelId}) but the stage is unavailable!")
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
        behaviorAttributes: LoriTuberItemBehaviorAttributes.Computer
    ): List<ObjectActionOption> {
        return listOf(ObjectActionOption.PlayOnSparklyPower)
    }

    data object BasicComputer : ComputerBehavior() {
        override val parallelTasks = 1
    }
}