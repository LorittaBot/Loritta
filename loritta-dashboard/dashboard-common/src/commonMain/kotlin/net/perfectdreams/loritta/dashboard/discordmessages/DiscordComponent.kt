package net.perfectdreams.loritta.dashboard.discordmessages

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(ComponentConditionalSerializer::class)
sealed class DiscordComponent {
    abstract val type: Int

    @Serializable
    data class DiscordActionRow(@EncodeDefault override val type: Int = 1, val components: List<DiscordComponent>) : DiscordComponent()

    @Serializable
    data class DiscordButton(
        @EncodeDefault override val type: Int = 2,
        val label: String,
        val style: Int, // We only support link buttons
        val url: String // Again, we only support link buttons
    ) : DiscordComponent()
}

// Custom serializer that handles the conditional deserialization based on the "type" field
object ComponentConditionalSerializer : JsonContentPolymorphicSerializer<DiscordComponent>(DiscordComponent::class) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<DiscordComponent> {
        return when (val type = element.jsonObject["type"]?.jsonPrimitive?.intOrNull) {
            1    -> DiscordComponent.DiscordActionRow.serializer()
            2    -> DiscordComponent.DiscordButton.serializer()
            else -> error("Unsupported component type $type")
        }
    }
}