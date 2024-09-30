package net.perfectdreams.loritta.lorituber.server.processors

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

        val mood = listOf(
            character.data.energyNeed,
            character.data.hungerNeed,
            character.data.funNeed,
            character.data.hygieneNeed,
            character.data.bladderNeed,
            character.data.socialNeed
        ).average()

        if (mood >= 50.0) {
            // Set our new task!
            character.setTask(LoriTuberTask.WorkingOnVideo(request.channelId, request.pendingVideoId))
            return StartWorkingOnPendingVideoResponse.Success
        } else {
            return StartWorkingOnPendingVideoResponse.MoodTooLow
        }
    }
}