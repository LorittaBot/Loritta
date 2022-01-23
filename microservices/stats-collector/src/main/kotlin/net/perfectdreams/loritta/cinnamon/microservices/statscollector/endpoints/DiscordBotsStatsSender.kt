package net.perfectdreams.loritta.cinnamon.microservices.statscollector.endpoints

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

class DiscordBotsStatsSender(
    private val http: HttpClient,
    private val clientId: Long,
    private val token: String
) : StatsSender {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun send(guildCount: Long) {
        val result = http.post<HttpResponse>("https://discord.bots.gg/api/v1/bots/$clientId/stats") {
            header("Authorization", token)
            accept(ContentType.Application.Json)
            body = TextContent(Json.encodeToString(UpdateBotStatsRequest(guildCount)), ContentType.Application.Json)
        }

        val response = result.readText()
        logger.info { "Discord Bots response: $response"}

        if (!result.status.isSuccess())
            throw RuntimeException("Discord Bots response wasn't a success!")
    }

    @Serializable
    data class UpdateBotStatsRequest(
        @SerialName("guildCount")
        val serverCount: Long
    )
}