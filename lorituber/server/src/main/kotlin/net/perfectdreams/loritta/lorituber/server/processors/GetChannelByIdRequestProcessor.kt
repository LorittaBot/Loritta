package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberChannels
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel
import net.perfectdreams.loritta.serializable.lorituber.requests.GetChannelByIdRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.GetChannelByIdResponse
import org.jetbrains.exposed.sql.selectAll

class GetChannelByIdRequestProcessor : LoriTuberRpcProcessor {
    suspend fun process(request: GetChannelByIdRequest, currentTick: Long, lastUpdate: Long): GetChannelByIdResponse {
        val channel = LoriTuberChannels.selectAll().where {
            LoriTuberChannels.id eq request.channelId
        }.firstOrNull() ?: return GetChannelByIdResponse(
            currentTick,
            lastUpdate,
            null
        )

        return GetChannelByIdResponse(
            currentTick,
            lastUpdate,
            LoriTuberChannel(
                channel[LoriTuberChannels.id].value,
                channel[LoriTuberChannels.name],
            )
        )
    }
}