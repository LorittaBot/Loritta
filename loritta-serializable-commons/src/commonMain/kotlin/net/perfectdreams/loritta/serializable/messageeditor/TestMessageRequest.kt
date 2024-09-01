package net.perfectdreams.loritta.serializable.messageeditor

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType

@Serializable
data class TestMessageRequest(
    val message: String,
    val channelId: Long?,
    val sectionType: PlaceholderSectionType,
    val placeholders: Map<String, String>
)