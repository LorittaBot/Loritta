package net.perfectdreams.loritta.morenitta.websiteinternal

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest

suspend fun main() {
    val http = HttpClient {}

    val a = http.post("http://127.0.0.1:13003/rpc") {
        setBody(
            Json.encodeToString<LorittaInternalRPCRequest>(
                LorittaInternalRPCRequest.GetGuildGamerSaferConfigRequest(268353819409252352, 123170274651668480)
            )
        )
    }

    println(a.bodyAsText())
}