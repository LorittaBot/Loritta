package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse

interface PacketProcessor<T : LoriTuberRequest> {
    suspend fun process(request: T): LoriTuberResponse
}