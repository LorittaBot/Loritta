package net.perfectdreams.loritta.cinnamon.discord.utils.correios

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosObjeto
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities.CorreiosResponse
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.exceptions.InvalidTrackingIdException
import net.perfectdreams.loritta.common.utils.JsonIgnoreUnknownKeys
import java.io.Closeable
import java.util.*
import kotlin.time.Duration.Companion.minutes

class CorreiosClient : Closeable {
    companion object {
        val CORREIOS_PACKAGE_REGEX = Regex("[A-Z]{2}[0-9]{9}[A-Z]{2}")
        private const val REQUEST_TOKEN = "YW5kcm9pZDtici5jb20uY29ycmVpb3MucHJlYXRlbmRpbWVudG87RjMyRTI5OTc2NzA5MzU5ODU5RTBCOTdGNkY4QTQ4M0I5Qjk1MzU3OA=="
        private const val USER_AGENT = "Dart/2.18 (dart:io)"
    }

    val http = HttpClient {
        expectSuccess = false
    }

    val scope = CoroutineScope(Dispatchers.IO)

    private var correiosToken: CorreiosToken? = null
    private var tokenMutex = Mutex()

    /**
     * Gets package tracking information about the [trackingIds]
     *
     * @param trackingIds list of tracking IDs, Correios doesn't seem to limit how many packages you can track at the same time
     * @return the request response
     * @throws InvalidTrackingIdException if any of the [trackingIds] do not match the [CORREIOS_PACKAGE_REGEX] RegEx
     */
    suspend fun getPackageInfo(vararg trackingIds: String): List<CorreiosObjeto> {
        // Validate tracking IDs
        for (trackingId in trackingIds)
            if (!trackingId.matches(CORREIOS_PACKAGE_REGEX))
                throw InvalidTrackingIdException(trackingId)

        // Based off https://github.com/FinotiLucas/Correios-Brasil because the old SRO Mobile API doesn't work anymore (big sad moment)
        val token = this.tokenMutex.withLock {
            val cachedToken = this.correiosToken
            if (cachedToken == null || Clock.System.now() + 2.minutes >= cachedToken.expiresAt) {
                val response = http.post("https://proxyapp.correios.com.br/v1/app-validation") {
                    userAgent(USER_AGENT)
                    setBody(
                        TextContent(
                            buildJsonObject {
                                put("requestToken", REQUEST_TOKEN)
                            }.toString(),
                            ContentType.Application.Json
                        )
                    )
                }

                val a = Json.parseToJsonElement(response.bodyAsText()).jsonObject

                val token = a["token"]!!.jsonPrimitive.content
                val tokenInfo = token.split(".")[1]
                val expiresAt = Instant.fromEpochSeconds(Json.parseToJsonElement(Base64.getDecoder().decode(tokenInfo).toString(Charsets.UTF_8)).jsonObject["exp"]!!.jsonPrimitive.long)

                val newToken = CorreiosToken(
                    token,
                    expiresAt
                )

                this.correiosToken = newToken
                return@withLock newToken
            }
            return@withLock cachedToken
        }

        val httpResponses = trackingIds.map {
            scope.async {
                http.get("https://proxyapp.correios.com.br/v1/sro-rastro/$it") {
                    userAgent(USER_AGENT)
                    accept(ContentType.Application.Json)
                    header("App-Check-Token", token.token)
                }
            }
        }

        val objetos = httpResponses.awaitAll().flatMap {
            JsonIgnoreUnknownKeys.decodeFromString<CorreiosResponse>(it.bodyAsText().also { println(it) })
                .objetos
        }

        return objetos
    }

    override fun close() {
        http.close()

        runBlocking {
            scope.cancel()
        }
    }

    data class CorreiosToken(
        val token: String,
        val expiresAt: Instant
    )
}