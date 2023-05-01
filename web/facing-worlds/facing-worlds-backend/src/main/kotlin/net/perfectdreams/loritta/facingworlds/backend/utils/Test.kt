package net.perfectdreams.loritta.facingworlds.backend.utils

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.facingworlds.common.v1.FacingWorldsRPCRequest
import net.perfectdreams.loritta.facingworlds.common.v1.PutPowerStreamClaimedFirstSonhosRewardRequest
import net.perfectdreams.loritta.facingworlds.common.v1.PutPowerStreamClaimedLimitedTimeSonhosRewardRequest

suspend fun main() {
    val http = HttpClient {}
    val r = if (true) {
        http.post("http://127.0.0.1:4569/v1/rpc") {
            setBody(
                Json.encodeToString<FacingWorldsRPCRequest>(
                    PutPowerStreamClaimedFirstSonhosRewardRequest(
                        123170274651668480L,
                        2_500,
                        0
                    )
                )
            )
        }
    } else {
        http.post("http://127.0.0.1:4569/v1/rpc") {
            setBody(
                Json.encodeToString<FacingWorldsRPCRequest>(
                    PutPowerStreamClaimedLimitedTimeSonhosRewardRequest(
                        123170274651668480L,
                        2_500,
                        0,
                        0
                    )
                )
            )
        }
    }
    println(r.status)
    println(r.bodyAsText())
}