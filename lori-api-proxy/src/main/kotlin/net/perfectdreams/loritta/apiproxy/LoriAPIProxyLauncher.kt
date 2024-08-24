package net.perfectdreams.loritta.apiproxy

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import kotlin.time.Duration.Companion.seconds

object LoriAPIProxyLauncher {
    private val logger = KotlinLogging.logger {}

    @JvmStatic
    fun main(args: Array<String>) {
        val http = HttpClient()

        val lorittaMainInstanceUrl = System.getProperty("proxy.lorittaMainInstanceUrl")
        val response = runBlocking { getLorittaReplicasInfo(lorittaMainInstanceUrl, http) }
        logger.info { "Loritta replica information: Environment Type: ${response.environmentType}; Max Shards: ${response.maxShards}; ${response.instances.size} replicas" }

        val m = LoriAPIProxy(http, response)
        m.start()
    }

    suspend fun getLorittaReplicasInfo(lorittaMainInstanceUrl: String, http: HttpClient): LorittaInternalRPCResponse.GetLorittaInfoResponse.Success {
        while (true) {
            try {
                logger.info { "Attempting to get Loritta's replicas info from Loritta's main replica..." }
                // Attempt to get Loritta's replicas info from the main replica

                return Json.decodeFromString<LorittaInternalRPCResponse>(
                    http.post("${lorittaMainInstanceUrl.removeSuffix("/")}/rpc") {
                        setBody(Json.encodeToString<LorittaInternalRPCRequest>(LorittaInternalRPCRequest.GetLorittaInfoRequest))
                    }.bodyAsText()
                ) as LorittaInternalRPCResponse.GetLorittaInfoResponse.Success
            } catch (e: Exception) {
                logger.warn(e) { "Failed to get Loritta's replicas info from Loritta's main replica! Trying again in 2s..." }
                delay(2.seconds)
            }
        }
    }
}