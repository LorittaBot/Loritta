package net.perfectdreams.loritta.serializable.messageeditor

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.placeholders.MessagePlaceholder
import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType
import net.perfectdreams.loritta.common.utils.placeholders.Placeholders
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.DiscordUser

@Serializable
data class LorittaDiscordMessageEditorSetupConfig(
    val templates: List<LorittaMessageTemplate>,
    val placeholderSectionType: PlaceholderSectionType,
    val placeholders: List<MessageEditorMessagePlaceholder>,
    val guild: DiscordGuild,
    val selfLorittaUser: DiscordUser,
    val testMessageTargetChannelQuery: TestMessageTargetChannelQuery,
    val testMessageEndpointUrl: String,
)

@Serializable
data class LorittaMessageTemplate(
    val name: String,
    val content: String
)

@Serializable
data class MessageEditorMessagePlaceholder(
    val name: String,
    // Yes, this means that technically someone could change what replacer is being used when testing a message on the dashboard
    // But does it really matter? The user can already type whatever they want in the box...
    val replaceWithBackend: String,
    val replaceWithFrontend: String,
    val renderType: MessagePlaceholder.RenderType
) {
    val asKey: String
        get() = Placeholders.createPlaceholderKey(name)
}

@Serializable
sealed class TestMessageTargetChannelQuery {
    @Serializable
    class QuerySelector(val querySelector: String) : TestMessageTargetChannelQuery()
    @Serializable
    data object SendDirectMessage : TestMessageTargetChannelQuery()
}