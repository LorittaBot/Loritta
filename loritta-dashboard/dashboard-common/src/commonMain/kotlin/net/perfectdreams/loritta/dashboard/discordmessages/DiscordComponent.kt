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

    @Serializable
    data class DiscordSection(
        @EncodeDefault override val type: Int = 9,
        val components: List<DiscordComponent>, // 1-3 Text Display components
        val accessory: DiscordComponent? = null // Button or Thumbnail
    ) : DiscordComponent()

    @Serializable
    data class DiscordTextDisplay(
        @EncodeDefault override val type: Int = 10,
        val content: String // Markdown text with token replacement support
    ) : DiscordComponent()

    @Serializable
    data class DiscordThumbnail(
        @EncodeDefault override val type: Int = 11,
        val media: UnfurledMediaItem, // Image URL with token replacement
        val description: String? = null, // Max 1024 chars, with token replacement
        val spoiler: Boolean = false
    ) : DiscordComponent()

    @Serializable
    data class DiscordMediaGallery(
        @EncodeDefault override val type: Int = 12,
        val items: List<MediaGalleryItem> // 1-10 items
    ) : DiscordComponent() {
        @Serializable
        data class MediaGalleryItem(
            val media: UnfurledMediaItem, // Media URL with token replacement
            val description: String? = null, // Max 1024 chars
            val spoiler: Boolean = false
        )
    }

    @Serializable
    data class UnfurledMediaItem(
        val url: String
    )

    @Serializable
    data class DiscordSeparator(
        @EncodeDefault override val type: Int = 14,
        val divider: Boolean = true,
        val spacing: Int = 1 // 1 = small, 2 = large
    ) : DiscordComponent()

    @Serializable
    data class DiscordContainer(
        @EncodeDefault override val type: Int = 17,
        val components: List<DiscordComponent>, // Nested components
        val accentColor: Int? = null, // RGB color 0x000000 to 0xFFFFFF
        val spoiler: Boolean = false
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
            9    -> DiscordComponent.DiscordSection.serializer()
            10   -> DiscordComponent.DiscordTextDisplay.serializer()
            11   -> DiscordComponent.DiscordThumbnail.serializer()
            12   -> DiscordComponent.DiscordMediaGallery.serializer()
            14   -> DiscordComponent.DiscordSeparator.serializer()
            17   -> DiscordComponent.DiscordContainer.serializer()
            else -> error("Unsupported component type $type")
        }
    }
}