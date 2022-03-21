package net.perfectdreams.loritta.cinnamon.microservices.statscollector.senders

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging

class TopggStatsSender(
    private val http: HttpClient,
    private val clientId: Long,
    private val token: String
) : StatsSender {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun send(guildCount: Long) {
        val result = http.post<HttpResponse>("https://top.gg/api/bots/$clientId/stats") {
            header("Authorization", token)
            accept(ContentType.Application.Json)
            body = TextContent(Json.encodeToString(UpdateBotStatsRequest(guildCount)), ContentType.Application.Json)
        }

        val response = result.readText()
        logger.info { "top.gg response: $response"}

        if (!result.status.isSuccess())
            throw RuntimeException("top.gg response wasn't a success!")
    }

    @Serializable
    data class UpdateBotStatsRequest(
        @SerialName("server_count")
        val serverCount: Long
    )
}