package net.perfectdreams.loritta.cinnamon.discord.gateway

import dev.kord.gateway.Event
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.utils.JsonIgnoreUnknownKeys

object KordDiscordEventUtils {
    private val logger = KotlinLogging.logger {}

    fun parseEventFromString(gatewayPayload: String) = parseEventFromJsonObject(
        Json.parseToJsonElement(gatewayPayload)
            .jsonObject
    )

    fun parseEventFromJsonObject(gatewayPayload: JsonObject): Event? {
        // Kord doesn't deserialize this well because it relies on the order
        try {
            return JsonIgnoreUnknownKeys.decodeFromJsonElement(
                Event.DeserializationStrategy,
                buildJsonObject {
                    gatewayPayload["op"]?.let {
                        put("op", it.jsonPrimitive.intOrNull)
                    }

                    gatewayPayload["t"]?.let {
                        put("t", it)
                    }

                    gatewayPayload["s"]?.let {
                        put("s", it)
                    }

                    gatewayPayload["d"]?.let {
                        put("d", it)
                    }
                }
            )
        } catch (e: Exception) {
            // This can throw...
            // SerializationException
            // NumberFormatException: For input string: "9033969170"
            logger.warn(e) { "Something went wrong while trying to deserialize $gatewayPayload" }
            return null
        }
    }
}