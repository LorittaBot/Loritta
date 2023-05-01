package net.perfectdreams.loritta.facingworlds.backend.rpc.processors

import io.ktor.http.*
import net.perfectdreams.loritta.facingworlds.common.v1.FacingWorldsRPCResponse

interface FacingWorldsRpcProcessor {
    data class ProcessorResponse(
        val status: HttpStatusCode,
        val response: FacingWorldsRPCResponse
    )
}