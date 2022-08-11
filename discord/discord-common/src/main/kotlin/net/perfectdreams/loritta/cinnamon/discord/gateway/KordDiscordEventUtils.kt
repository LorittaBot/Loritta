package net.perfectdreams.loritta.cinnamon.discord.gateway

import dev.kord.gateway.Event
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.utils.JsonIgnoreUnknownKeys

object KordDiscordEventUtils {
    private val logger = KotlinLogging.logger {}

    fun parseEventFromString(gatewayPayload: String) = parseEventFromJsonObject(
        Json.parseToJsonElement(gatewayPayload)
            .jsonObject
    )

    fun parseEventFromJsonObject(gatewayPayload: JsonObject): Event? {
        // kotlinx.serialization doesn't deserialize this well because it relies on the order
        // Using decodeFromJsonElement crashes with "Index -1 out of bounds for length 0", why?
        try {
            return JsonIgnoreUnknownKeys.decodeFromString(
                Event.DeserializationStrategy,
                buildJsonObject {
                    gatewayPayload["op"]?.let {
                        put("op", it.jsonPrimitive.longOrNull)
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
                }.toString()
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