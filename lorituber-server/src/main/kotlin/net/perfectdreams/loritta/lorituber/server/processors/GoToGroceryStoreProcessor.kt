package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.GoToGroceryStoreRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.GoToGroceryStoreResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer

class GoToGroceryStoreProcessor(val m: LoriTuberServer) : PacketProcessor<GoToGroceryStoreRequest> {
    override suspend fun process(request: GoToGroceryStoreRequest): LoriTuberResponse {
        val worldTime = m.gameState.getWorldTime()

        // Let's bypass for now
        if (false && worldTime.hours !in 7..18)
            return GoToGroceryStoreResponse.Closed

        return GoToGroceryStoreResponse.Success(
            m.gameState.nelsonGroceryStore.items.map { it.data }
        )
    }
}