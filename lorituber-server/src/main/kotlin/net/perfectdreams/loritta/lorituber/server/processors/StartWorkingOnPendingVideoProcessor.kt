package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.LoriTuberVideoStage
import net.perfectdreams.loritta.lorituber.bhav.LoriTuberItemBehaviorAttributes
import net.perfectdreams.loritta.lorituber.bhav.UseItemAttributes
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.rpc.packets.StartWorkingOnPendingVideoRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.StartWorkingOnPendingVideoResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer

class StartWorkingOnPendingVideoProcessor(val m: LoriTuberServer) : PacketProcessor<StartWorkingOnPendingVideoRequest> {
    override suspend fun process(request: StartWorkingOnPendingVideoRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }

        val channel = m.gameState.channels.firstOrNull {
            it.id == request.channelId
        }

        if (channel == null)
            return StartWorkingOnPendingVideoResponse.UnknownChannel

        val pendingVideo = channel.data.pendingVideos.firstOrNull { it.id == request.pendingVideoId }
        if (pendingVideo == null)
            return StartWorkingOnPendingVideoResponse.UnknownPendingVideo

        // You need to have a computer...
        //
        // Get the first computer
        val computer = character.data.items.firstOrNull { it.behaviorAttributes is LoriTuberItemBehaviorAttributes.Computer }
        val behaviorAttributes = computer?.behaviorAttributes as? LoriTuberItemBehaviorAttributes.Computer

        if (computer == null || behaviorAttributes == null)
            error("User does not have a computer!")

        // TODO: Do not let someone use the computer while it is already being used
        // TODO: Do not let work on a stage that is unavailable/finished

        if (request.stage == LoriTuberVideoStage.RENDERING) {
            // Rendering stage is a bit different
            // You CAN render stuff in your computer while you are AFK
            behaviorAttributes.videoRenderTask = LoriTuberItemBehaviorAttributes.Computer.RenderingVideo(
                channel.id,
                pendingVideo.id
            )

            return StartWorkingOnPendingVideoResponse.Success
        } else {
            if (character.motives.isMoodAboveRequiredForWork()) {
                if (behaviorAttributes.videoRenderTask != null)
                    error("You can't use the computer while you are rendering a video!")

                // Set our new task!
                character.setTask(
                    LoriTuberTask.UsingItem(
                        computer.localId,
                        UseItemAttributes.Computer.WorkOnVideo(
                            request.channelId,
                            request.pendingVideoId,
                            request.stage
                        )
                    )
                )

                return StartWorkingOnPendingVideoResponse.Success
            } else {
                return StartWorkingOnPendingVideoResponse.MoodTooLow
            }
        }
    }
}