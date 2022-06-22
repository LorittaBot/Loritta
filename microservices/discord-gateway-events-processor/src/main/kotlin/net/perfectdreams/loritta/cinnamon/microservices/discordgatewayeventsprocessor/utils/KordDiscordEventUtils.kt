package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import dev.kord.gateway.Event
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import mu.KotlinLogging

object KordDiscordEventUtils {
    private val json = Json { ignoreUnknownKeys = true }
    private val logger = KotlinLogging.logger {}

    fun parseEventFromJsonString(gatewayPayload: String): Event? {
        // TODO: Ktor doesn't deserialize this well because it relies on the order
        val gatewayPayloadStuff = Json.parseToJsonElement(gatewayPayload)
            .jsonObject

        // Using decodeFromJsonElement crashes with "Index -1 out of bounds for length 0", why?
        try {
            return json.decodeFromString(
                Event.DeserializationStrategy,
                buildJsonObject {
                    gatewayPayloadStuff["op"]?.let {
                        put("op", it.jsonPrimitive.longOrNull)
                    }

                    gatewayPayloadStuff["t"]?.let {
                        put("t", it)
                    }

                    gatewayPayloadStuff["s"]?.let {
                        put("s", it)
                    }

                    gatewayPayloadStuff["d"]?.let {
                        put("d", it)
                    }
                }.toString()
            )
        } catch (e: SerializationException) {
            logger.warn(e) { "Something went wrong while trying to deserialize $gatewayPayload" }
            return null
        }
    }
}