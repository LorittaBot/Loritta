package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.GetWorldInfoRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GetWorldInfoResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.tables.LoriTuberWorldTicks
import org.jetbrains.exposed.sql.selectAll

class GetWorldInfoProcessor(val m: LoriTuberServer) : PacketProcessor<GetWorldInfoRequest> {
    override suspend fun process(request: GetWorldInfoRequest): LoriTuberResponse {
        val data = m.transaction {
            LoriTuberWorldTicks.selectAll()
                .where {
                    LoriTuberWorldTicks.type eq request.worldName
                }
                .first()
        }

        return GetWorldInfoResponse(
            m.gameState.worldInfo.currentTick,
            m.gameState.worldInfo.lastUpdate,
            m.averageTickDurations.map { it.inWholeMilliseconds }.average()
        )
    }
}