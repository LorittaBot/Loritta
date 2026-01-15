package net.perfectdreams.loritta.dashboard.discordmessages

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
data class DiscordMessage(
    val content: String? = null,
    val tts: Boolean = false,
    val embeds: List<DiscordEmbed>? = null,
    val components: List<DiscordComponent>? = null,
    val flags: Int = 0
) {
    companion object {
        val JsonForDiscordMessages = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        fun decodeFromJsonString(input: String): DiscordMessage? {
            return try {
                // We COULD make a custom serializable...
                // But honestly? That's a bit tricky because we only want to remap ONE specific field that may be named "embed" OR "embeds" :(
                val messageAsMap = JsonForDiscordMessages.parseToJsonElement(input).jsonObject.toMutableMap()
                val embed = messageAsMap["embed"]
                if (embed != null) {
                    messageAsMap.remove("embed")
                    messageAsMap["embeds"] = JsonArray(listOf(embed))
                }

                JsonForDiscordMessages.decodeFromJsonElement<DiscordMessage>(JsonObject(messageAsMap))
            } catch (e: SerializationException) {
                null
            } catch (e: IllegalStateException) {
                null // This may be triggered when a message has invalid message components
            } catch (e: IllegalArgumentException) {
                null // This may be triggered when a message has invalid message components²
            }
        }
    }
}