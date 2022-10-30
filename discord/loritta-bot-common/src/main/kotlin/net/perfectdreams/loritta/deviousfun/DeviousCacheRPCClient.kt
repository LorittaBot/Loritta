package net.perfectdreams.loritta.deviousfun

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.DeviousRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import org.apache.http.ssl.SSLContexts
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class DeviousCacheRPCClient(val url: String) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val http = HttpClient(Java) {
        engine {
            threadsCount = 256
            pipelining = true

            config {
                version(java.net.http.HttpClient.Version.HTTP_2)
                sslContext(insecureContext())
            }
        }
    }

    private fun insecureContext(): SSLContext? {
        val noopTrustManager = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(xcs: Array<X509Certificate?>?, string: String?) {}
                override fun checkServerTrusted(xcs: Array<X509Certificate?>?, string: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate>? {
                    return null
                }
            }
        )

        val sc = SSLContext.getInstance("ssl")
        sc.init(null, noopTrustManager, null)
        return sc
    }

    suspend fun execute(request: DeviousRequest): DeviousResponse {
        logger.debug { "Sending ${request::class.simpleName} to DeviousCache" }

        return Json.decodeFromString<DeviousResponse>(
            http.post("https://$url/rpc") {
                setBody(Json.encodeToString<DeviousRequest>(request))
            }.also { logger.debug { "RPC Result: ${it.status}" } }.bodyAsText()
        )
    }

    fun unknownResponse(response: DeviousResponse): Nothing = error("I don't know how to handle ${response::class}")
}