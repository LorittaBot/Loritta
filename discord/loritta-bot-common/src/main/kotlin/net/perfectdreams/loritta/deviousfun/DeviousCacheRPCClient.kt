package net.perfectdreams.loritta.deviousfun

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.requests.DeviousRequest
import net.perfectdreams.loritta.deviouscache.responses.DeviousResponse
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class DeviousCacheRPCClient(val url: String) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val maxThreads = 48

    // Before we were using the "Java" engine, but depending on the thread count, there is a "too many streams" error
    // So now we are using OkHttp
    // https://stackoverflow.com/questions/54917885/java-11-httpclient-http2-too-many-streams-error
    val http = HttpClient(OkHttp) {
        engine {
            // Ktor gets a "slice" of the Dispatchers.IO executor based off this number
            threadsCount = maxThreads

            config {
                val naiveTrustManager = object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
                    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
                }

                val insecureSocketFactory = SSLContext.getInstance("TLSv1.2").apply {
                    val trustAllCerts = arrayOf<TrustManager>(naiveTrustManager)
                    init(null, trustAllCerts, SecureRandom())
                }.socketFactory

                sslSocketFactory(insecureSocketFactory, naiveTrustManager)
                hostnameVerifier { _, _ -> true }

                dispatcher(
                    Dispatcher().apply {
                        this.maxRequestsPerHost = maxThreads
                        this.maxRequests = maxThreads
                    }
                )
            }
        }
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