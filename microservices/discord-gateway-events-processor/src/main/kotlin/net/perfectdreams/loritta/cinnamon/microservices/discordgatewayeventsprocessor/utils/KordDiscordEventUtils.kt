package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils

import dev.kord.gateway.Event
import kotlinx.serialization.json.*

object KordDiscordEventUtils {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseEventFromJsonString(gatewayPayload: String): Event? {
        // TODO: Ktor doesn't deserialize this well because it relies on the order
        val gatewayPayloadStuff = Json.parseToJsonElement(gatewayPayload)
            .jsonObject

        // Using decodeFromJsonElement crashes with "Index -1 out of bounds for length 0", why?
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
    }
}