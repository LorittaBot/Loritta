package net.perfectdreams.loritta.cinnamon.discord.utils.falatron

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.loritta.common.utils.JsonIgnoreUnknownKeys
import java.io.Closeable
import java.util.*
import kotlin.time.Duration.Companion.minutes

class Falatron(private val apiUrl: String, private val apiKey: String) : Closeable {
    private val http = HttpClient {
        install(HttpTimeout) {
            connectTimeoutMillis = 180_000
            requestTimeoutMillis = 180_000
            socketTimeoutMillis  = 180_000
        }
    }

    /**
     * Generates an audio clip via Falatron
     *
     * @return the audio in MP3 format
     */
    suspend fun generate(voice: String, text: String, queuePositionCallback: (Int) -> (Unit)): ByteArray {
        val response = withContext(Dispatchers.IO) {
            tryAndRepeatGenerationRequest(voice, text)
        }

        val falatronResponse = Json.decodeFromString<FalatronTaskRequestResponse>(response)

        val voiceResponse = withContext(Dispatchers.IO) {
            tryAndRepeatTaskCheck(falatronResponse.taskId, queuePositionCallback)
        }

        return Base64.getDecoder().decode(voiceResponse.voice)
    }

    private suspend fun tryAndRepeatGenerationRequest(voice: String, text: String): String {
        val request = FalatronRequest(voice, text)
        var i = 0
        while (i != 5) {
            // println("tryAndRepeat")

            val response = withTimeout(3.minutes) {
                http.post(apiUrl) {
                    header("x-api-key", apiKey)
                    setBody(
                        TextContent(
                            Json.encodeToString(request),
                            ContentType.Application.Json
                        )
                    )
                }
            }

            // println("tryAndRepeat ${response.bodyAsText()}")

            if (response.status.isSuccess())
                return response.bodyAsText()

            i++
        }

        error("Failed to get Falatron voice after 5 tries!")
    }

    private suspend fun tryAndRepeatTaskCheck(taskId: String, queuePositionCallback: (Int) -> (Unit)): FalatronResponse {
        var i = 0
        while (i != 60) { // 5 minutes
            // println("tryAndRepeatTask $taskId")
            val response = withTimeout(3.minutes) {
                http.get("$apiUrl/$taskId") {
                    header("x-api-key", apiKey)
                }
            }

            val json = Json.parseToJsonElement(response.bodyAsText())
                .jsonObject

            if (json.containsKey("erro"))
                error("Something went wrong while getting Falatron's response! ${json["erro"]}")

            val body = response.bodyAsText()
            // println("tryAndRepeatTask $taskId ${response.bodyAsText()}")
            if (response.status == HttpStatusCode.OK)
                return JsonIgnoreUnknownKeys.decodeFromString<FalatronResponse>(body)
            if (response.status == HttpStatusCode.Accepted) {
                val b = JsonIgnoreUnknownKeys.decodeFromString<FalatronTaskRequestCheckResponse>(body)
                queuePositionCallback.invoke(b.queue)
            } else {
                // We will only increase the "i" if it wasn't accepted, if it was, then it means we are on the queue and it should hopefully be generated after a while
                i++
            }

            delay(5_000) // This one has delay because it needs to generate the voice / Cris recommended to check every 5s
        }

        error("Failed to get Falatron voice after 60 tries!")
    }

    override fun close() {
        http.close()
    }

    @Serializable
    data class FalatronRequest(
        val voz: String,
        val texto: String
    )

    @Serializable
    data class FalatronTaskRequestResponse(
        val queue: Int,
        @SerialName("task_id")
        val taskId: String // This is a UUID
    )

    @Serializable
    data class FalatronTaskRequestCheckResponse(
        val queue: Int,
        val status: String
    )

    @Serializable
    data class FalatronResponse(val voice: String)
}