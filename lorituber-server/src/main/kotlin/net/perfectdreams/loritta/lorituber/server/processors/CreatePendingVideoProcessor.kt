package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.LoriTuberVideoStage
import net.perfectdreams.loritta.lorituber.rpc.packets.CreatePendingVideoRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.CreatePendingVideoResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberPendingVideoData
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberPendingVideoStageData

class CreatePendingVideoProcessor(val m: LoriTuberServer) : PacketProcessor<CreatePendingVideoRequest> {
    override suspend fun process(request: CreatePendingVideoRequest): LoriTuberResponse {
        val channel = m.gameState.channels.firstOrNull {
            it.id == request.channelId
        }

        if (channel == null)
            return CreatePendingVideoResponse.UnknownChannel

        if (channel.data.pendingVideos.isNotEmpty())
            return CreatePendingVideoResponse.CharacterIsAlreadyDoingAnotherVideo

        val character = m.gameState.characters.first { it.id == channel.data.characterId }

        val contentCategoryLevel = channel.data.categoryLevels[request.contentCategory] ?: 1

        // We coerce between 11 and 1_000 because when the user is a lil baby with only one level, it WILL have a "0" maxCategoryLevelValue, and we don't
        // want that
        // maxCategoryLevelValue - 10 = 1

        val maxCategoryLevelValue = (20 * (contentCategoryLevel / 2)).coerceIn(11, 1_000)

        val pendingVideoData = LoriTuberPendingVideoData(
            channel.nextPendingVideoId(),
            request.contentCategory,
            request.contentVibes,
            LoriTuberPendingVideoStageData.Finished(m.gameState.random.nextInt(maxCategoryLevelValue - 10, maxCategoryLevelValue)),
            LoriTuberPendingVideoStageData.InProgress(0),
            LoriTuberPendingVideoStageData.Unavailable,
            LoriTuberPendingVideoStageData.Unavailable,
            LoriTuberPendingVideoStageData.Unavailable,
        )

        channel.data.pendingVideos.add(pendingVideoData)
        character.setTask(LoriTuberTask.WorkingOnVideo(channel.id, pendingVideoData.id, LoriTuberVideoStage.RECORDING))

        channel.isDirty = true

        return CreatePendingVideoResponse.Success
    }
}