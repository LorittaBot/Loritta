package net.perfectdreams.loritta.website.backend

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.loritta.serializable.ApplicationCommandInfo
import net.perfectdreams.loritta.serializable.CommandInfo
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class Commands(val websiteUrl: String, val http: HttpClient) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    lateinit var legacyCommandsInfo: List<CommandInfo>
    lateinit var applicationCommandsInfo: List<ApplicationCommandInfo>

    suspend fun start() {
        logger.info { "Querying Loritta commands..." }
        try {
            val messageCommandListRequest = http.get(websiteUrl.removeSuffix("/") + "/api/v1/loritta/commands/default")
            legacyCommandsInfo = Json.decodeFromString<List<CommandInfo>>(messageCommandListRequest.bodyAsText())

            val applicationCommandListRequest =
                http.get(websiteUrl.removeSuffix("/") + "/api/v1/loritta/application-commands")
            applicationCommandsInfo =
                Json.decodeFromString<List<ApplicationCommandInfo>>(applicationCommandListRequest.bodyAsText())
        } catch (e: Exception) {
            logger.warn(e) { "Failed to query Loritta commands! Retrying in 5s..." }
            delay(5.seconds)
            start()
            return
        }

        logger.info { "Successfully queried Loritta commands!" }

        // This is used to keep Loritta's commands always up to date
        GlobalScope.launch {
            delay(1.minutes)
            start()
        }
    }
}